package com.abhi1kush.positionanalyser.dao.review;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobVO;

@Repository
public class InMemoryReviewJobDao implements ReviewJobDao {

	private final Map<String, ReviewJobVO> jobs = new ConcurrentHashMap<>();

	@Override
	public void save(ReviewJobVO job) {
		jobs.put(job.getId(), job);
	}

	@Override
	public ReviewJobVO findById(String id) {
		return jobs.get(id);
	}

	@Override
	public Collection<ReviewJobVO> findAll() {
		return jobs.values();
	}
}
