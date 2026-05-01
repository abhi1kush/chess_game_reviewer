package com.abhi1kush.positionanalyser.valuebeans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PositionSummaryAnalysis {

	private final String fen;
	private final String sideToMove;
	private final String gameStatus;
	private final boolean inCheck;
	private final int legalMoveCount;
	private final int materialWhite;
	private final int materialBlack;
	private final int materialDifference;
}
