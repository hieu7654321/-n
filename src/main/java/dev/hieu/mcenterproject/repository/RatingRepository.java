package dev.hieu.mcenterproject.repository;

import dev.hieu.mcenterproject.model.Rating;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends MongoRepository<Rating, String> {
    List<Rating> findByUserId(String userId);
    List<Rating> findByTeacherId(String TeacherId);
    List<Rating> findByUserIdAndTeacherId(String userId, String TeacherId);
}
