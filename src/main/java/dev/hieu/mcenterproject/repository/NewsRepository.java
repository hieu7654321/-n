package dev.hieu.mcenterproject.repository;

import dev.hieu.mcenterproject.model.News;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NewsRepository extends MongoRepository<News, String> {
}
