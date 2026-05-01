package com.abhi1kush.positionanalyser.api;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.abhi1kush.positionanalyser.chess.ChessPositionService;

@WebMvcTest(controllers = ChessPositionController.class)
@Import({ ChessPositionService.class, GlobalExceptionHandler.class })
class ChessPositionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void legalMovesReturnsMovesForStartingPosition() throws Exception {
		mockMvc.perform(get("/api/chess/legal-moves"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fen").exists())
				.andExpect(jsonPath("$.legalMovesUci").isArray())
				.andExpect(jsonPath("$.legalMovesUci.length()").value(20));
	}

	@Test
	void legalMovesReturns400ForInvalidFen() throws Exception {
		mockMvc.perform(get("/api/chess/legal-moves").param("fen", "not-a-fen"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void analysisSummaryReturnsSnapshot() throws Exception {
		mockMvc.perform(get("/api/chess/analysis/summary"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sideToMove").value("WHITE"))
				.andExpect(jsonPath("$.legalMoveCount").value(20))
				.andExpect(jsonPath("$.gameStatus").value("ONGOING"))
				.andExpect(jsonPath("$.materialWhite").value(39))
				.andExpect(jsonPath("$.materialBlack").value(39))
				.andExpect(jsonPath("$.materialDifference").value(0));
	}

	@Test
	void analysisDetailedReturnsFullPayload() throws Exception {
		mockMvc.perform(post("/api/chess/analysis/detailed")
						.contentType(APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.legalMovesUci").isArray())
				.andExpect(jsonPath("$.legalMovesUci.length()").value(20))
				.andExpect(jsonPath("$.pieceCountsWhite").exists())
				.andExpect(jsonPath("$.pieceCountsBlack").exists())
				.andExpect(jsonPath("$.castlingRights").exists())
				.andExpect(jsonPath("$.enPassantSquare").exists());
	}
}
