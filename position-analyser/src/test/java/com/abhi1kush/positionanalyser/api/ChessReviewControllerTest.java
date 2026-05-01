package com.abhi1kush.positionanalyser.api;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.abhi1kush.positionanalyser.service.engine.ChessEngineService;
import com.abhi1kush.positionanalyser.dao.review.InMemoryReviewJobDao;
import com.abhi1kush.positionanalyser.service.review.FenSequenceBuilder;
import com.abhi1kush.positionanalyser.service.review.GameReviewJobService;
import com.abhi1kush.positionanalyser.service.review.GameReviewService;
import com.abhi1kush.positionanalyser.service.review.MoveClassifier;
import com.abhi1kush.positionanalyser.service.review.ReviewSummaryBuilder;
import com.abhi1kush.positionanalyser.controller.ChessReviewController;
import com.abhi1kush.positionanalyser.controller.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ChessReviewController.class)
@Import({
		GameReviewJobService.class,
		GameReviewService.class,
		FenSequenceBuilder.class,
		MoveClassifier.class,
		ReviewSummaryBuilder.class,
		InMemoryReviewJobDao.class,
		GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
		"review.job.maxConcurrent=1",
		"review.engine.depth.quick=8",
		"review.engine.depth.standard=12",
		"review.engine.depth.deep=16"
})
class ChessReviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ChessEngineService chessEngineService;

	@Test
	void quickReviewReturnsComputedMoveList() throws Exception {
		when(chessEngineService.analyze(anyString(), anyInt(), anyBoolean()))
				.thenReturn(new ChessEngineService.EngineAnalysis("e2e4", 40, null, Collections.singletonList("e2e4")))
				.thenReturn(new ChessEngineService.EngineAnalysis("e7e5", 20, null, Collections.emptyList()))
				.thenReturn(new ChessEngineService.EngineAnalysis("g1f3", 10, null, Collections.emptyList()))
				.thenReturn(new ChessEngineService.EngineAnalysis("b8c6", 60, null, Collections.emptyList()));

		mockMvc.perform(post("/api/review/quick")
						.contentType(APPLICATION_JSON)
						.content("{\"movesUci\":[\"e2e4\",\"e7e5\"]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("completed"))
				.andExpect(jsonPath("$.moves").isArray())
				.andExpect(jsonPath("$.moves.length()").value(2))
				.andExpect(jsonPath("$.summary").exists());
	}

	@Test
	void jobLifecycleEndpointsReturnStatusAndResult() throws Exception {
		when(chessEngineService.analyze(anyString(), anyInt(), anyBoolean()))
				.thenReturn(new ChessEngineService.EngineAnalysis("e2e4", 40, null, Collections.singletonList("e2e4")))
				.thenReturn(new ChessEngineService.EngineAnalysis("e7e5", 20, null, Collections.emptyList()))
				.thenReturn(new ChessEngineService.EngineAnalysis("g1f3", 10, null, Collections.emptyList()))
				.thenReturn(new ChessEngineService.EngineAnalysis("b8c6", 60, null, Collections.emptyList()));

		MvcResult createResult = mockMvc.perform(post("/api/review/jobs")
						.contentType(APPLICATION_JSON)
						.content("{\"movesUci\":[\"e2e4\",\"e7e5\"],\"analysisProfile\":\"quick\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.jobId").exists())
				.andReturn();

		JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
		String jobId = createJson.get("jobId").asText();

		for (int i = 0; i < 20; i++) {
			MvcResult statusResult = mockMvc.perform(get("/api/review/jobs/{jobId}", jobId))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status", anyOf(is("queued"), is("running"), is("completed"), is("failed"))))
					.andReturn();
			String status = objectMapper.readTree(statusResult.getResponse().getContentAsString()).get("status").asText();
			if ("completed".equals(status)) {
				break;
			}
			Thread.sleep(50L);
		}

		mockMvc.perform(get("/api/review/jobs/{jobId}/result", jobId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("completed"))
				.andExpect(jsonPath("$.moves").isArray());
	}
}
