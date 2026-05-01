package com.abhi1kush.positionanalyser.service.review;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.abhi1kush.positionanalyser.valuebeans.review.GameReviewSummaryVO;
import com.abhi1kush.positionanalyser.valuebeans.review.MoveReviewVO;

@Component
public class ReviewSummaryBuilder {

	public GameReviewSummaryVO build(List<MoveReviewVO> moveReviews) {
		List<Integer> critical = new ArrayList<>();
		int whiteCount = 0;
		int blackCount = 0;
		int whiteScore = 0;
		int blackScore = 0;

		for (MoveReviewVO review : moveReviews) {
			int moveScore = scoreFromCpl(review.getCpl() != null ? review.getCpl() : 0);
			if ("WHITE".equals(review.getSide())) {
				whiteCount++;
				whiteScore += moveScore;
			} else if ("BLACK".equals(review.getSide())) {
				blackCount++;
				blackScore += moveScore;
			}
			if (review.getCpl() != null && review.getCpl() > 100) {
				critical.add(review.getPly());
			}
		}

		Integer accuracyWhite = whiteCount > 0 ? whiteScore / whiteCount : null;
		Integer accuracyBlack = blackCount > 0 ? blackScore / blackCount : null;
		return new GameReviewSummaryVO(accuracyWhite, accuracyBlack, "Unknown", critical);
	}

	private int scoreFromCpl(int cpl) {
		int score = 100 - (cpl / 3);
		if (score < 0) {
			return 0;
		}
		if (score > 100) {
			return 100;
		}
		return score;
	}
}
