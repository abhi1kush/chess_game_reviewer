package com.abhi1kush.positionanalyser.valuebeans;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LegalMovesResponse {

	private final String fen;
	private final List<String> legalMovesUci;
}
