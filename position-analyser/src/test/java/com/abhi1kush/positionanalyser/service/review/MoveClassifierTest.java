package com.abhi1kush.positionanalyser.service.review;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.abhi1kush.positionanalyser.service.review.MoveClassifier;

class MoveClassifierTest {

	private final MoveClassifier classifier = new MoveClassifier();

	@Test
	void classifyUsesConfiguredThresholdBands() {
		assertEquals("best", classifier.classify(5));
		assertEquals("excellent", classifier.classify(20));
		assertEquals("good", classifier.classify(45));
		assertEquals("inaccuracy", classifier.classify(80));
		assertEquals("mistake", classifier.classify(160));
		assertEquals("blunder", classifier.classify(400));
	}
}
