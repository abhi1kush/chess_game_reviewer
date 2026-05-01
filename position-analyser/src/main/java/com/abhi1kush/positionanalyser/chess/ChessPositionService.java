package com.abhi1kush.positionanalyser.chess;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.abhi1kush.positionanalyser.dto.LegalMovesResponse;
import com.abhi1kush.positionanalyser.dto.PositionDetailedAnalysis;
import com.abhi1kush.positionanalyser.dto.PositionSummaryAnalysis;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Constants;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

@Service
public class ChessPositionService {

	public LegalMovesResponse legalMoves(String fen) {
		Board board = loadBoard(fen);
		List<Move> moves = legalMovesOrThrow(board);
		List<String> uci = moves.stream().map(Move::toString).collect(Collectors.toList());
		return new LegalMovesResponse(board.getFen(), uci);
	}

	public PositionSummaryAnalysis analyzeSummary(String fen) {
		Board board = loadBoard(fen);
		List<Move> moves = legalMovesOrThrow(board);
		int mw = material(board, Side.WHITE);
		int mb = material(board, Side.BLACK);
		return new PositionSummaryAnalysis(
				board.getFen(),
				board.getSideToMove().name(),
				gameStatus(board),
				board.isKingAttacked(),
				moves.size(),
				mw,
				mb,
				mw - mb);
	}

	public PositionDetailedAnalysis analyzeDetailed(String fen) {
		Board board = loadBoard(fen);
		List<Move> moves = legalMovesOrThrow(board);
		List<String> uci = moves.stream().map(Move::toString).collect(Collectors.toList());
		int mw = material(board, Side.WHITE);
		int mb = material(board, Side.BLACK);
		Square ep = board.getEnPassant();
		String epStr = (ep == null || ep == Square.NONE) ? "-" : ep.toString().toLowerCase();
		String[] fenParts = board.getFen().split(" ");
		String castling = fenParts.length > 2 ? fenParts[2] : "-";
		return new PositionDetailedAnalysis(
				board.getFen(),
				board.getSideToMove().name(),
				gameStatus(board),
				board.isKingAttacked(),
				moves.size(),
				mw,
				mb,
				mw - mb,
				uci,
				board.getHalfMoveCounter() != null ? board.getHalfMoveCounter() : 0,
				board.getMoveCounter() != null ? board.getMoveCounter() : 1,
				castling,
				epStr,
				pieceCounts(board, Side.WHITE),
				pieceCounts(board, Side.BLACK));
	}

	private static List<Move> legalMovesOrThrow(Board board) {
		try {
			return MoveGenerator.generateLegalMoves(board);
		} catch (MoveGeneratorException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	private Board loadBoard(String fen) {
		String effective = (fen == null || fen.trim().isEmpty()) ? Constants.startStandardFENPosition : fen.trim();
		try {
			Board board = new Board();
			board.loadFromFen(effective);
			return board;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Invalid FEN or chess position", e);
		}
	}

	private static String gameStatus(Board board) {
		if (board.isMated()) {
			return "CHECKMATE";
		}
		if (board.isStaleMate()) {
			return "STALEMATE";
		}
		if (board.isDraw()) {
			return "DRAW";
		}
		if (board.isKingAttacked()) {
			return "CHECK";
		}
		return "ONGOING";
	}

	private static int material(Board board, Side side) {
		int sum = 0;
		for (Piece p : board.boardToArray()) {
			if (p == Piece.NONE || p.getPieceSide() != side) {
				continue;
			}
			sum += pieceValue(p.getPieceType());
		}
		return sum;
	}

	private static int pieceValue(PieceType pt) {
		if (pt == null || pt == PieceType.NONE) {
			return 0;
		}
		switch (pt) {
			case PAWN:
				return 1;
			case KNIGHT:
			case BISHOP:
				return 3;
			case ROOK:
				return 5;
			case QUEEN:
				return 9;
			case KING:
				return 0;
			default:
				return 0;
		}
	}

	private static Map<String, Integer> pieceCounts(Board board, Side side) {
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (Piece p : board.boardToArray()) {
			if (p == Piece.NONE || p.getPieceSide() != side) {
				continue;
			}
			String key = p.getPieceType().name();
			Integer cur = counts.get(key);
			counts.put(key, cur == null ? 1 : cur + 1);
		}
		return counts;
	}
}
