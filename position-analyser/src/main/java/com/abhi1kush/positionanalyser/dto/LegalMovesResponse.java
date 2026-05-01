package com.abhi1kush.positionanalyser.dto;

import java.util.List;

public class LegalMovesResponse {

	private final String fen;
	private final List<String> legalMovesUci;

	public LegalMovesResponse(String fen, List<String> legalMovesUci) {
		this.fen = fen;
		this.legalMovesUci = legalMovesUci;
	}

	public String getFen() {
		return fen;
	}

	public List<String> getLegalMovesUci() {
		return legalMovesUci;
	}
}
