package com.abhi1kush.positionanalyser.dao.review;

import java.util.Collection;

import com.abhi1kush.positionanalyser.valuebeans.review.ReviewJobVO;

public interface ReviewJobDao {

	void save(ReviewJobVO job);

	ReviewJobVO findById(String id);

	Collection<ReviewJobVO> findAll();
}
