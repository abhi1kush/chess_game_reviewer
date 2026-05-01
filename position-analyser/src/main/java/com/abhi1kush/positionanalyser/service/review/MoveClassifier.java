package com.abhi1kush.positionanalyser.service.review;

import org.springframework.stereotype.Component;

@Component
public class MoveClassifier {

	public String classify(int cpl) {
		if (cpl <= 10) {
			return "best";
		}
		if (cpl <= 30) {
			return "excellent";
		}
		if (cpl <= 60) {
			return "good";
		}
		if (cpl <= 100) {
			return "inaccuracy";
		}
		if (cpl <= 250) {
			return "mistake";
		}
		return "blunder";
	}
}
