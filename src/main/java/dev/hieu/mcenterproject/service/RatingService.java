package dev.hieu.mcenterproject.service;

import dev.hieu.mcenterproject.model.Rating;
import dev.hieu.mcenterproject.repository.RatingRepository;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }


    public double calculateAverageRating(String teacherId) {
        return ratingRepository.findByTeacherId(teacherId)
                .stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);
    }

    public int countTotalReviews(String teacherId) {
        return ratingRepository.findByTeacherId(teacherId).size();
    }
}

