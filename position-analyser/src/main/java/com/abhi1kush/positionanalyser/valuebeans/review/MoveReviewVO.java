package com.abhi1kush.positionanalyser.valuebeans.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MoveReviewVO {

	private final int ply;
	private final String side;
	private final String fenBefore;
	private final String playedMoveUci;
	private final String bestMoveUci;
	private final Integer evalBeforeCp;
	private final Integer evalAfterCp;
	private final Integer cpl;
	private final String classification;
	private final int legalMoveCount;
	private final List<String> tags;
	private final List<String> pv;
}
