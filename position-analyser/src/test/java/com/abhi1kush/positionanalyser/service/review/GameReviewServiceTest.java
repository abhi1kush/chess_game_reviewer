package com.abhi1kush.positionanalyser.service.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.abhi1kush.positionanalyser.service.engine.ChessEngineService;
import com.abhi1kush.positionanalyser.valuebeans.review.ReviewResultVO;
import com.abhi1kush.positionanalyser.valuebeans.review.AnalysisProfile;
import com.abhi1kush.positionanalyser.valuebeans.review.SideFocus;

class GameReviewServiceTest {

	@Test
	void analyzeGameBuildsPerMoveReviewAndSummary() {
		ChessEngineService engine = Mockito.mock(ChessEngineService.class);
		when(engine.analyze(anyString(), anyInt(), anyBoolean()))
				.thenReturn(new ChessEngineService.EngineAnalysis("e2e4", 40, null, Collections.singletonList("e2e4 e7e5")))
				.thenReturn(new ChessEngineService.EngineAnalysis("e7e5", 20, null, Collections.emptyList()))
				.thenReturn(new ChessEngineService.EngineAnalysis("g1f3", 10, null, Collections.emptyList()))
				.thenReturn(new ChessEngineService.EngineAnalysis("b8c6", 80, null, Collections.emptyList()));

		GameReviewService service = new GameReviewService(
				engine,
				new FenSequenceBuilder(),
				new MoveClassifier(),
				new ReviewSummaryBuilder(),
				8,
				12,
				16);

		ReviewResultVO result = service.analyzeGame(
				null,
				Arrays.asList("e2e4", "e7e5"),
				AnalysisProfile.QUICK,
				SideFocus.BOTH,
				true,
				true,
				null);

		assertEquals(2, result.getMoveReviews().size());
		assertEquals("WHITE", result.getMoveReviews().get(0).getSide());
		assertFalse(result.getMoveReviews().get(0).getPv().isEmpty());
		assertEquals("excellent", result.getMoveReviews().get(0).getClassification());
		assertEquals("inaccuracy", result.getMoveReviews().get(1).getClassification());
		assertEquals(Integer.valueOf(94), result.getSummary().getAccuracyWhite());
	}
}
