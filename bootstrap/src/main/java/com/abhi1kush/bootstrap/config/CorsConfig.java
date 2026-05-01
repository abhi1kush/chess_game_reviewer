package com.abhi1kush.bootstrap.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

	@Bean
	public WebMvcConfigurer corsConfigurer(CorsProperties corsProperties) {
		return new WebMvcConfigurer() {
			@Override
			public void configurePathMatch(PathMatchConfigurer configurer) {
				configurer.setUseTrailingSlashMatch(true);
			}

			@Override
			public void addCorsMappings(CorsRegistry registry) {
				List<String> patterns = corsProperties.getAllowedOriginPatterns();
				if (patterns == null || patterns.isEmpty()) {
					return;
				}
				registry.addMapping("/api/**")
						.allowedOriginPatterns(patterns.toArray(String[]::new))
						.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
			}
		};
	}
}
