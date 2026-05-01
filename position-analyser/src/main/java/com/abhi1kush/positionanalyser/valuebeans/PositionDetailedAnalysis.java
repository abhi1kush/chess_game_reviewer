package com.abhi1kush.positionanalyser.valuebeans;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
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
	private final String engineBestMoveUci;
	private final Integer engineScoreCp;
	private final Integer engineMateIn;
}
