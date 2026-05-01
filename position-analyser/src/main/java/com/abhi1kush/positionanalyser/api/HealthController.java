package com.abhi1kush.positionanalyser.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/position-analyser")
public class HealthController {

	@GetMapping("/health")
	public Map<String, String> health() {
		Map<String, String> body = new HashMap<>();
		body.put("status", "UP");
		body.put("module", "position-analyser");
		return body;
	}
}
