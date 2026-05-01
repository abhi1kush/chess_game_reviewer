package com.abhi1kush.positionanalyser.dto;

import java.util.List;
import java.util.Map;

public class PositionDetailedAnalysis {

	private final String fen;
	private final String sideToMove;
	private final String gameStatus;
	private final boolean inCheck;
	private final int legalMoveCount;
	private final int materialWhite;
	private final int materialBlack;
	private final int materialDifference;
	private final List<String> legalMovesUci;
	private final int halfMoveClock;
	private final int fullMoveNumber;
	private final String castlingRights;
	private final String enPassantSquare;
	private final Map<String, Integer> pieceCountsWhite;
	private final Map<String, Integer> pieceCountsBlack;

	public PositionDetailedAnalysis(
			String fen,
			String sideToMove,
			String gameStatus,
			boolean inCheck,
			int legalMoveCount,
			int materialWhite,
			int materialBlack,
			int materialDifference,
			List<String> legalMovesUci,
			int halfMoveClock,
			int fullMoveNumber,
			String castlingRights,
			String enPassantSquare,
			Map<String, Integer> pieceCountsWhite,
			Map<String, Integer> pieceCountsBlack) {
		this.fen = fen;
		this.sideToMove = sideToMove;
		this.gameStatus = gameStatus;
		this.inCheck = inCheck;
		this.legalMoveCount = legalMoveCount;
		this.materialWhite = materialWhite;
		this.materialBlack = materialBlack;
		this.materialDifference = materialDifference;
		this.legalMovesUci = legalMovesUci;
		this.halfMoveClock = halfMoveClock;
		this.fullMoveNumber = fullMoveNumber;
		this.castlingRights = castlingRights;
		this.enPassantSquare = enPassantSquare;
		this.pieceCountsWhite = pieceCountsWhite;
		this.pieceCountsBlack = pieceCountsBlack;
	}

	public String getFen() {
		return fen;
	}

	public String getSideToMove() {
		return sideToMove;
	}

	public String getGameStatus() {
		return gameStatus;
	}

	public boolean isInCheck() {
		return inCheck;
	}

	public int getLegalMoveCount() {
		return legalMoveCount;
	}

	public int getMaterialWhite() {
		return materialWhite;
	}

	public int getMaterialBlack() {
		return materialBlack;
	}

	public int getMaterialDifference() {
		return materialDifference;
	}

	public List<String> getLegalMovesUci() {
		return legalMovesUci;
	}

	public int getHalfMoveClock() {
		return halfMoveClock;
	}

	public int getFullMoveNumber() {
		return fullMoveNumber;
	}

	public String getCastlingRights() {
		return castlingRights;
	}

	public String getEnPassantSquare() {
		return enPassantSquare;
	}

	public Map<String, Integer> getPieceCountsWhite() {
		return pieceCountsWhite;
	}

	public Map<String, Integer> getPieceCountsBlack() {
		return pieceCountsBlack;
	}
}
