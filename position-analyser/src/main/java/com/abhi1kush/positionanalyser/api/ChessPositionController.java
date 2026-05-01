package com.abhi1kush.positionanalyser.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abhi1kush.positionanalyser.chess.ChessPositionService;
import com.abhi1kush.positionanalyser.dto.FenRequest;
import com.abhi1kush.positionanalyser.dto.LegalMovesResponse;
import com.abhi1kush.positionanalyser.dto.PositionDetailedAnalysis;
import com.abhi1kush.positionanalyser.dto.PositionSummaryAnalysis;

@RestController
@RequestMapping("/api/chess")
public class ChessPositionController {

	private final ChessPositionService chessPositionService;

	public ChessPositionController(ChessPositionService chessPositionService) {
		this.chessPositionService = chessPositionService;
	}

	@GetMapping("/legal-moves")
	public LegalMovesResponse legalMoves(@RequestParam(required = false) String fen) {
		return chessPositionService.legalMoves(fen);
	}

	@GetMapping({ "/analysis/summary", "/analysis/summary/" })
	public PositionSummaryAnalysis analysisSummaryGet(@RequestParam(required = false) String fen) {
		return chessPositionService.analyzeSummary(fen);
	}

	@PostMapping({ "/analysis/summary", "/analysis/summary/" })
	public PositionSummaryAnalysis analysisSummaryPost(@RequestBody(required = false) FenRequest body) {
		String fen = body != null ? body.getFen() : null;
		return chessPositionService.analyzeSummary(fen);
	}

	@PostMapping("/analysis/detailed")
	public PositionDetailedAnalysis analysisDetailed(@RequestBody(required = false) FenRequest body) {
		String fen = body != null ? body.getFen() : null;
		return chessPositionService.analyzeDetailed(fen);
	}
}
