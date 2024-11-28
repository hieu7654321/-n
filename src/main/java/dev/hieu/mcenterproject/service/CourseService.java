//package dev.hieu.mcenterproject.service;
//
//import dev.hieu.mcenterproject.model.Course;
//import dev.hieu.mcenterproject.model.User;
//import dev.hieu.mcenterproject.repository.CourseRepository;
//import dev.hieu.mcenterproject.repository.UserRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@AllArgsConstructor
//public class CourseService {
//
//    private final CourseRepository courseRepository;
//
//    private final UserRepository userRepository;
//
//    public void addCourse(Course course) {
//        courseRepository.save(course);
//    }
//
//    public List<Course> findAllCourses() {
//        return courseRepository.findAll();
//    }
//
//    public void assignStudentToCourse(String studentId, String courseId) {
//        Optional<User> student = userRepository.findById(studentId);
//        Optional<Course> course = courseRepository.findById(courseId);
//        if (student.isPresent() && course.isPresent()) {
//            course.get().getStudents().add(student.get());
//            courseRepository.save(course.get());
//        }
//    }
//    public boolean registerStudentToCourse(String username, Long courseId) {
//        Optional<Course> courseOpt = courseRepository.findById(String.valueOf(courseId));
//        Optional<User> studentOpt = userRepository.findByUsername(username);
//
//        if (courseOpt.isPresent() && studentOpt.isPresent()) {
//            Course course = courseOpt.get();
//            User student = studentOpt.get();
//
//            // Check if the course is full
//            if (course.getStudents().size() < course.getMaxStudents()) {
//                course.getStudents().add(student);
//                courseRepository.save(course);
//                return true;  // Successfully registered
//            }
//        }
//        return false;  // Course is full or other error
//    }
//    public List<Course> findCoursesByTeacherUsername(String teacherUsername) {
//        return courseRepository.findByTeacherUsername(teacherUsername);
//    }
//
//    public Course findById(String courseId) {
//        Optional<Course> course = courseRepository.findById(courseId);
//        return course.orElseThrow(() -> new RuntimeException("Course not found"));
//    }
//}
//
package dev.hieu.mcenterproject.service;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.Course;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.CourseRepository;
import dev.hieu.mcenterproject.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByTeacher(String teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public Optional<Course> getCourseById(String courseId) {
        return courseRepository.findById(courseId);
    }

    public String registerStudent(String courseId, String studentId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        Optional<User> studentOpt = userRepository.findById(studentId);

        if (courseOpt.isEmpty() || studentOpt.isEmpty()) {
            return "Course or student not found.";
        }

        Course course = courseOpt.get();
        User student = studentOpt.get();

        if (course.getStudents().contains(studentId)) {
            return "You are already registered for this course.";
        }

        if (course.getStudents().size() >= course.getCapacity()) {
            return "This course is full.";
        }

        course.getStudents().add(studentId);
        student.getCourses().add(courseId);

        courseRepository.save(course);
        userRepository.save(student);

        return "Successfully registered for the course!";
    }

    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    public String assignStudentToCourse(String courseId, String studentId) {
        return registerStudent(courseId, studentId);
    }

//    public String assignTeacherToCourse(String courseId, String teacherId) {
//        Optional<Course> courseOpt = courseRepository.findById(courseId);
//        Optional<User> teacherOpt = userRepository.findById(teacherId);
//
//        if (courseOpt.isEmpty() || teacherOpt.isEmpty()) {
//            return "Course or teacher not found.";
//        }
//
//        Course course = courseOpt.get();
//        course.setTeacherId(teacherId);
//        courseRepository.save(course);
//
//        return "Teacher assigned successfully!";
//    }

//    public List<Course> getAvailableCoursesForRegistration() {
//        return courseRepository.findByApprovedTrueAndCompletedFalse();
//    }

    public void registerStudentToCourse(String courseId, String studentId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));
        if (course.getEnrolledStudents().contains(studentId) || course.getCurrentCapacity() >= course.getMaxCapacity()) {
            throw new IllegalStateException("Cannot register student.");
        }
        course.getEnrolledStudents().add(studentId);
        course.setCurrentCapacity(course.getCurrentCapacity() + 1);
        courseRepository.save(course);
    }

    public void assignTeacherToCourse(String courseId, String teacherId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));
        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new IllegalArgumentException("Invalid teacher ID"));

        if (teacher.getRole() == UsersRole.TEACHER) {
            course.setTeacherId(teacherId);
            courseRepository.save(course);
        } else {
            throw new IllegalStateException("User is not a teacher.");
        }
    }
}
