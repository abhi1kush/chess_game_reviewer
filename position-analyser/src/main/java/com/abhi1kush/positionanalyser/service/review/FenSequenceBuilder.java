package com.abhi1kush.positionanalyser.service.review;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Constants;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

@Component
public class FenSequenceBuilder {

	private static final Pattern HEADER_PATTERN = Pattern.compile("(?s)\\[[^\\]]*\\]");
	private static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)\\{[^\\}]*\\}");

	public List<Move> parseMoves(String pgn, List<String> movesUci) {
		if (movesUci != null && !movesUci.isEmpty()) {
			return parseFromUci(movesUci);
		}
		if (pgn == null || pgn.trim().isEmpty()) {
			throw new IllegalArgumentException("Either pgn or movesUci must be provided.");
		}
		return parseFromPgnSan(pgn);
	}

	private List<Move> parseFromUci(List<String> movesUci) {
		Board board = new Board();
		board.loadFromFen(Constants.startStandardFENPosition);
		List<Move> moves = new ArrayList<>();
		for (String uci : movesUci) {
			if (uci == null || uci.trim().isEmpty()) {
				continue;
			}
			Move matched = findLegalMove(board, uci.trim().toLowerCase());
			if (matched == null) {
				throw new IllegalArgumentException("Illegal or invalid UCI move in sequence: " + uci);
			}
			moves.add(matched);
			board.doMove(matched);
		}
		if (moves.isEmpty()) {
			throw new IllegalArgumentException("movesUci is empty.");
		}
		return moves;
	}

	private Move findLegalMove(Board board, String uci) {
		try {
			for (Move legal : MoveGenerator.generateLegalMoves(board)) {
				if (uci.equals(legal.toString())) {
					return legal;
				}
			}
			return null;
		} catch (MoveGeneratorException e) {
			throw new IllegalArgumentException("Could not generate legal moves for current position.", e);
		}
	}

	private List<Move> parseFromPgnSan(String pgn) {
		String sanitized = sanitizePgnMoveText(pgn);
		MoveList moveList = new MoveList(Constants.startStandardFENPosition);
		try {
			moveList.loadFromSan(sanitized);
		} catch (MoveConversionException e) {
			throw new IllegalArgumentException("Invalid PGN move text.", e);
		}
		return new ArrayList<>(moveList);
	}

	private String sanitizePgnMoveText(String pgn) {
		String noHeaders = HEADER_PATTERN.matcher(pgn).replaceAll(" ");
		String noComments = COMMENT_PATTERN.matcher(noHeaders).replaceAll(" ");
		return noComments.replaceAll("\\d+\\.(\\.\\.)?", " ")
				.replaceAll("1-0|0-1|1/2-1/2|\\*", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}

	public void validateMoveSequence(List<Move> moves) {
		Board board = new Board();
		board.loadFromFen(Constants.startStandardFENPosition);
		for (Move move : moves) {
			if (!board.isMoveLegal(move, true)) {
				throw new IllegalArgumentException("Illegal move in input sequence: " + move.toString());
			}
			board.doMove(move);
		}
	}
}
