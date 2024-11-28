package dev.hieu.mcenterproject.repository;

import dev.hieu.mcenterproject.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    Optional<Course> findById(String id);
    List<Course> findByTeacherId(String teacherId);
    List<Course> findByEnrolledStudentsContainingAndApprovedAndCompleted(String studentId, Boolean approved, Boolean completed);
    List<Course> findByEnrolledStudentsContaining(String studentId);
    // Tìm các khóa học của giảng viên dựa trên teacherId
    List<Course> findByTeacherIdAndApprovedAndCompleted(String teacherId, Boolean approved, Boolean completed);
//    List<Course> findCompletedCoursesByStudent(String studentId);
}
