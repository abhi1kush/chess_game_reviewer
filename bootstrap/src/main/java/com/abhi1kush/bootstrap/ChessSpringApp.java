package com.abhi1kush.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.abhi1kush")
public class ChessSpringApp {
	public static void main(String[] args) {
		SpringApplication.run(ChessSpringApp.class, args);
	}
}
