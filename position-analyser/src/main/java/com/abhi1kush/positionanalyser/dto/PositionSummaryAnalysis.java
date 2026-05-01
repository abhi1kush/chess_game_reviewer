package com.abhi1kush.positionanalyser.dto;

public class PositionSummaryAnalysis {

	private final String fen;
	private final String sideToMove;
	private final String gameStatus;
	private final boolean inCheck;
	private final int legalMoveCount;
	private final int materialWhite;
	private final int materialBlack;
	private final int materialDifference;

	public PositionSummaryAnalysis(
			String fen,
			String sideToMove,
			String gameStatus,
			boolean inCheck,
			int legalMoveCount,
			int materialWhite,
			int materialBlack,
			int materialDifference) {
		this.fen = fen;
		this.sideToMove = sideToMove;
		this.gameStatus = gameStatus;
		this.inCheck = inCheck;
		this.legalMoveCount = legalMoveCount;
		this.materialWhite = materialWhite;
		this.materialBlack = materialBlack;
		this.materialDifference = materialDifference;
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
}
