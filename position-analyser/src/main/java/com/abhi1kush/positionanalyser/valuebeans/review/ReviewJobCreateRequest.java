package com.abhi1kush.positionanalyser.valuebeans.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewJobCreateRequest {

	private String pgn;
	private List<String> movesUci;
	private String analysisProfile;
	private String sideFocus;
}
