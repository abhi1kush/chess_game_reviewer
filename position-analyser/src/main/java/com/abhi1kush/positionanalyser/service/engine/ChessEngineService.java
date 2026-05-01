package com.abhi1kush.positionanalyser.service.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChessEngineService {

	private final String host;
	private final int port;
	private final int depth;
	private final int connectTimeoutMs;
	private final int readTimeoutMs;
	private final int poolSize;
	private final int borrowTimeoutMs;
	private final boolean reviewCacheEnabled;
	private final long reviewCacheTtlSeconds;
	private final BlockingQueue<EngineClient> clientPool;
	private final StringRedisTemplate redisTemplate;

	public ChessEngineService(
			@Value("${stockfish.host:localhost}") String host,
			@Value("${stockfish.port:8088}") int port,
			@Value("${stockfish.depth:12}") int depth,
			@Value("${stockfish.connect-timeout-ms:2000}") int connectTimeoutMs,
			@Value("${stockfish.read-timeout-ms:8000}") int readTimeoutMs,
			@Value("${stockfish.pool.size:2}") int poolSize,
			@Value("${stockfish.pool.borrow-timeout-ms:3000}") int borrowTimeoutMs,
			@Value("${review.cache.enabled:true}") boolean reviewCacheEnabled,
			@Value("${review.cache.ttlSeconds:604800}") long reviewCacheTtlSeconds,
			@Autowired(required = false) StringRedisTemplate redisTemplate) {
		this.host = host;
		this.port = port;
		this.depth = depth;
		this.connectTimeoutMs = connectTimeoutMs;
		this.readTimeoutMs = readTimeoutMs;
		this.poolSize = Math.max(1, poolSize);
		this.borrowTimeoutMs = Math.max(500, borrowTimeoutMs);
		this.reviewCacheEnabled = reviewCacheEnabled;
		this.reviewCacheTtlSeconds = Math.max(60L, reviewCacheTtlSeconds);
		this.redisTemplate = redisTemplate;
		this.clientPool = new ArrayBlockingQueue<>(this.poolSize);
		for (int i = 0; i < this.poolSize; i++) {
			this.clientPool.add(new EngineClient());
		}
	}

	public EngineAnalysis analyze(String fen) {
		return analyze(fen, depth, false);
	}

	public EngineAnalysis analyze(String fen, int analysisDepth, boolean includePv) {
		if (fen == null || fen.trim().isEmpty()) {
			throw new IllegalArgumentException("FEN must not be empty.");
		}
		final int effectiveDepth = analysisDepth > 0 ? analysisDepth : depth;
		final String normalizedFen = fen.trim();

		EngineAnalysis cached = getCached(normalizedFen, effectiveDepth, includePv);
		if (cached != null) {
			return cached;
		}

		EngineClient client = borrowClient();
		try {
			EngineAnalysis analysis = client.analyze(normalizedFen, effectiveDepth, includePv);
			putCached(normalizedFen, effectiveDepth, analysis);
			return analysis;
		} catch (RuntimeException first) {
			client.resetConnection();
			try {
				EngineAnalysis analysis = client.analyze(normalizedFen, effectiveDepth, includePv);
				putCached(normalizedFen, effectiveDepth, analysis);
				return analysis;
			} catch (RuntimeException second) {
				throw new IllegalStateException("Failed to query Stockfish engine", second);
			}
		} finally {
			returnClient(client);
		}
	}

	private EngineClient borrowClient() {
		try {
			EngineClient client = clientPool.poll(borrowTimeoutMs, TimeUnit.MILLISECONDS);
			if (client == null) {
				throw new IllegalStateException("Engine pool is busy. Try again shortly.");
			}
			return client;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for engine worker.", e);
		}
	}

	private void returnClient(EngineClient client) {
		if (client != null) {
			clientPool.offer(client);
		}
	}

	private EngineAnalysis getCached(String fen, int depth, boolean includePv) {
		if (!reviewCacheEnabled || redisTemplate == null) {
			return null;
		}
		String payload = redisTemplate.opsForValue().get(cacheKey(fen, depth));
		if (payload == null) {
			return null;
		}
		EngineAnalysis parsed = deserialize(payload);
		if (parsed == null) {
			return null;
		}
		if (includePv) {
			return parsed;
		}
		return new EngineAnalysis(parsed.getBestMoveUci(), parsed.getScoreCp(), parsed.getMateIn(), Collections.<String>emptyList());
	}

	private void putCached(String fen, int depth, EngineAnalysis analysis) {
		if (!reviewCacheEnabled || redisTemplate == null || analysis == null) {
			return;
		}
		redisTemplate.opsForValue().set(cacheKey(fen, depth), serialize(analysis), Duration.ofSeconds(reviewCacheTtlSeconds));
	}

	private String cacheKey(String fen, int depth) {
		return "engine:" + depth + ":" + fen;
	}

	private String serialize(EngineAnalysis analysis) {
		String pv = analysis.getPv() == null || analysis.getPv().isEmpty() ? "" : String.join(",", analysis.getPv());
		return safe(analysis.getBestMoveUci()) + "\t" + safeInt(analysis.getScoreCp()) + "\t" + safeInt(analysis.getMateIn()) + "\t" + pv;
	}

	private EngineAnalysis deserialize(String payload) {
		String[] parts = payload.split("\t", -1);
		if (parts.length < 4) {
			return null;
		}
		String best = emptyToNull(parts[0]);
		Integer cp = parseNullableInt(parts[1]);
		Integer mate = parseNullableInt(parts[2]);
		List<String> pv = parts[3].isEmpty() ? Collections.<String>emptyList() : Arrays.asList(parts[3].split(","));
		return new EngineAnalysis(best, cp, mate, pv);
	}

	private String safe(String value) {
		return value == null ? "" : value;
	}

	private String safeInt(Integer value) {
		return value == null ? "" : String.valueOf(value.intValue());
	}

	private Integer parseNullableInt(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private String emptyToNull(String value) {
		return value == null || value.isEmpty() ? null : value;
	}

	@PreDestroy
	public void shutdown() {
		for (EngineClient client : clientPool) {
			client.close();
		}
	}

	public static class EngineAnalysis {
		private final String bestMoveUci;
		private final Integer scoreCp;
		private final Integer mateIn;
		private final List<String> pv;

		public EngineAnalysis(String bestMoveUci, Integer scoreCp, Integer mateIn) {
			this(bestMoveUci, scoreCp, mateIn, Collections.<String>emptyList());
		}

		public EngineAnalysis(String bestMoveUci, Integer scoreCp, Integer mateIn, List<String> pv) {
			this.bestMoveUci = bestMoveUci;
			this.scoreCp = scoreCp;
			this.mateIn = mateIn;
			this.pv = pv != null ? pv : Collections.<String>emptyList();
		}

		public String getBestMoveUci() {
			return bestMoveUci;
		}

		public Integer getScoreCp() {
			return scoreCp;
		}

		public Integer getMateIn() {
			return mateIn;
		}

		public List<String> getPv() {
			return pv;
		}
	}

	private static class Score {
		private final Integer scoreCp;
		private final Integer mateIn;

		private Score(Integer scoreCp, Integer mateIn) {
			this.scoreCp = scoreCp;
			this.mateIn = mateIn;
		}
	}

	private class EngineClient {
		private Socket socket;
		private BufferedWriter writer;
		private BufferedReader reader;
		private boolean initialized;

		synchronized EngineAnalysis analyze(String fen, int analysisDepth, boolean includePv) {
			try {
				ensureConnected();
				send("isready");
				readUntil("readyok");
				send("position fen " + fen);
				send("go depth " + analysisDepth);

				Integer scoreCp = null;
				Integer mateIn = null;
				String bestMove = null;
				List<String> pv = Collections.<String>emptyList();
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("info ")) {
						Score score = extractScore(line);
						if (score != null) {
							if (score.scoreCp != null) {
								scoreCp = score.scoreCp;
							}
							if (score.mateIn != null) {
								mateIn = score.mateIn;
							}
						}
						if (includePv) {
							List<String> parsedPv = extractPv(line);
							if (!parsedPv.isEmpty()) {
								pv = parsedPv;
							}
						}
					}
					if (line.startsWith("bestmove ")) {
						String[] parts = line.split("\\s+");
						if (parts.length > 1) {
							bestMove = parts[1];
						}
						break;
					}
				}
				return new EngineAnalysis(bestMove, scoreCp, mateIn, pv);
			} catch (IOException e) {
				resetConnection();
				throw new IllegalStateException("Failed engine worker query", e);
			}
		}

		synchronized void resetConnection() {
			close();
			socket = null;
			writer = null;
			reader = null;
			initialized = false;
		}

		synchronized void close() {
			try {
				if (writer != null) {
					writer.write("quit");
					writer.newLine();
					writer.flush();
				}
			} catch (IOException ignored) {
				// ignore best-effort close
			}
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ignored) {
				// ignore best-effort close
			}
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ignored) {
				// ignore best-effort close
			}
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException ignored) {
				// ignore best-effort close
			}
		}

		private void ensureConnected() throws IOException {
			if (socket != null && socket.isConnected() && !socket.isClosed() && initialized) {
				return;
			}
			resetConnection();
			socket = new Socket();
			socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
			socket.setSoTimeout(readTimeoutMs);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
			send("uci");
			readUntil("uciok");
			send("isready");
			readUntil("readyok");
			send("ucinewgame");
			initialized = true;
		}

		private void send(String command) throws IOException {
			writer.write(command);
			writer.newLine();
			writer.flush();
		}

		private void readUntil(String expectedToken) throws IOException {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(expectedToken)) {
					return;
				}
			}
			throw new IOException("Stockfish stream ended before token: " + expectedToken);
		}

		private List<String> extractPv(String line) {
			int pvIndex = line.indexOf(" pv ");
			if (pvIndex < 0) {
				return Collections.<String>emptyList();
			}
			String pvPart = line.substring(pvIndex + 4).trim();
			if (pvPart.isEmpty()) {
				return Collections.<String>emptyList();
			}
			return Arrays.asList(pvPart.split("\\s+"));
		}

		private Score extractScore(String line) {
			String[] parts = line.split("\\s+");
			for (int i = 0; i < parts.length - 2; i++) {
				if ("score".equals(parts[i]) && "cp".equals(parts[i + 1])) {
					try {
						return new Score(Integer.valueOf(parts[i + 2]), null);
					} catch (NumberFormatException e) {
						return null;
					}
				}
				if ("score".equals(parts[i]) && "mate".equals(parts[i + 1])) {
					try {
						return new Score(null, Integer.valueOf(parts[i + 2]));
					} catch (NumberFormatException e) {
						return null;
					}
				}
			}
			return null;
		}
	}
}
