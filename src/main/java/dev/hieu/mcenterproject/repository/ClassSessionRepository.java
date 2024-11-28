package dev.hieu.mcenterproject.repository;

import dev.hieu.mcenterproject.model.ClassSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClassSessionRepository extends MongoRepository<ClassSession, String> {
    List<ClassSession> findByStudentIdsContaining(String studentIds);
}
