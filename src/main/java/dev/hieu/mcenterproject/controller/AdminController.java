package dev.hieu.mcenterproject.controller;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.ClassSession;
import dev.hieu.mcenterproject.model.Course;
import dev.hieu.mcenterproject.model.News;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.ClassSessionRepository;
import dev.hieu.mcenterproject.repository.CourseRepository;
import dev.hieu.mcenterproject.repository.NewsRepository;
import dev.hieu.mcenterproject.repository.UserRepository;
import dev.hieu.mcenterproject.service.CourseService;
import dev.hieu.mcenterproject.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final NewsRepository newsRepository;
    private final ClassSessionRepository classSessionRepository;

    @GetMapping("/admin/addCourse")
    public String showAddCourseForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        return "addCourse";
    }

    @PostMapping("/admin/addCourse")
    public String addCourse(
            @RequestParam("courseName") String name,
            @RequestParam("room") String room,
            @RequestParam("classSize") int maxCapacity,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("courseDate") String courseDate,
            @RequestParam("classFee") int classFee,
            @RequestParam("level") String level) {

        // Format schedule: "StartTime - EndTime, Date"
        String schedule = startTime + " - " + endTime + ", " + courseDate;

        Course course = Course.builder()
                .name(name)
                .room(room)
                .maxCapacity(maxCapacity)
                .currentCapacity(0)
                .schedule(schedule)
                .level(level)
                .fee(classFee)
                .enrolledStudents(new ArrayList<>())
                .build();

        courseRepository.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/admin/courses")
    public String showCourses(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "courseList"; // Tạo view để hiển thị danh sách khóa học
    }


    // Hiển thị chi tiết lớp học
    @GetMapping("/admin/course/{courseId}")
    public String showCourseDetails(@PathVariable String courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            model.addAttribute("error", "Course not found.");
            return "redirect:/admin/courses";
        }

        List<User> enrolledStudents = userRepository.findAllById(course.getEnrolledStudents());
        model.addAttribute("course", course);
        model.addAttribute("students", enrolledStudents);
        return "courseDetails"; // Tạo view để hiển thị chi tiết khóa học
    }

    // Xóa học viên khỏi lớp
    @PostMapping("/admin/course/{courseId}/removeStudent")
    public String removeStudent(@PathVariable String courseId, @RequestParam String studentId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.getEnrolledStudents().remove(studentId);
            course.setCurrentCapacity(course.getCurrentCapacity() - 1);
            courseRepository.save(course);
        }
        return "redirect:/admin/course/" + courseId; // Quay lại chi tiết khóa học
    }

    // Hiển thị form thêm học viên vào lớp
    @GetMapping("/admin/course/{courseId}/assignStudent")
    public String assignStudentForm(@PathVariable String courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            model.addAttribute("error", "Course not found.");
            return "redirect:/admin/courses";
        }

        UsersRole role = UsersRole.STUDENT; // Thay đổi giá trị này tùy theo yêu cầu
        List<User> students = userRepository.findAllByRole(role); // Lấy danh sách học viên
        model.addAttribute("course", course);
        model.addAttribute("students", students);
        return "assignStudents"; // Tạo view để hiển thị form thêm học viên
    }

    // Thêm học viên vào lớp
    @PostMapping("/admin/course/{courseId}/addStudent")
    public String addStudent(@PathVariable String courseId, @RequestParam String studentId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        User student = userRepository.findById(studentId).orElse(null);

        if (course != null && student != null) {
            // Kiểm tra nếu học viên đã được đăng ký
            if (!course.getEnrolledStudents().contains(studentId) && course.getCurrentCapacity() < course.getMaxCapacity()) {
                course.getEnrolledStudents().add(studentId);
                course.setCurrentCapacity(course.getCurrentCapacity() + 1);
                courseRepository.save(course);
            }
        }
        return "redirect:/admin/course/" + courseId; // Quay lại chi tiết khóa học
    }


    @PostMapping("/admin/news/add")
    public String addNews(@RequestParam("title") String title,
                          @RequestParam("content") String content) {
        News news = News.builder()
                .title(title)
                .content(content)
                .publishDate(LocalDate.now())
                .build();
        newsRepository.save(news);
        return "redirect:/event";
    }


    @PostMapping("/admin/news/edit")
    public String editNews(@RequestParam("newsId") String newsId,
                           @RequestParam("title") String title,
                           @RequestParam("content") String content) {
        News news = newsRepository.findById(newsId).orElse(null);
        if (news != null) {
            news.setTitle(title);
            news.setContent(content);
            newsRepository.save(news);
        }
        return "redirect:/event";
    }


    @PostMapping("/admin/news/delete")
    public String deleteNews(@RequestParam("newsId") String newsId) {
        newsRepository.deleteById(newsId);
        return "redirect:/event";
    }

    @PostMapping("/admin/course/approve/{courseId}")
    public String approveCourse(@PathVariable String courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            course.setApproved(true);
            courseRepository.save(course);
        }

//        String[] timeParts = course.getSchedule().split(",")[0].trim().split(" - ");
//        String startTime = timeParts[0]; //
//        String endTime = timeParts[1];
//        String courseDate = course.getSchedule().split(",")[1].trim();
//
//        String timeOfDay;
//        int startHour = Integer.parseInt(startTime.split(":")[0]);
//        if (startHour < 12) {
//            timeOfDay = "Sáng";
//        } else if (startHour < 18) {
//            timeOfDay = "Chiều";
//        } else {
//            timeOfDay = "Tối";
//        }
//
//        ClassSession classSession = ClassSession.builder()
//                .courseId(course.getId())
//                .date(course.getSchedule())
//                .timeOfDay(timeOfDay)
//                .teacherId(course.getTeacherId())
//                .studentIds(course.getEnrolledStudents())
//                .status('false')
//                .build();
//
//        classSessionRepository.save(classSession);
        return "redirect:/courses";
    }

    @GetMapping("/admin/course/{courseId}/assignTeacher")
    public String assignTeacherForm(@PathVariable String courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            model.addAttribute("error", "Course not found.");
            return "redirect:/admin/courses";
        }

        UsersRole role = UsersRole.TEACHER;
        List<User> teachers = userRepository.findAllByRole(role);
        model.addAttribute("course", course);
        model.addAttribute("teachers", teachers);
        return "assignTeachers";
    }


    @PostMapping("/admin/course/{courseId}/addTeacher")
    public String assignTeacher(@PathVariable String courseId, @RequestParam String teacherId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        User teacher = userRepository.findById(teacherId).orElse(null);

        if (course != null && teacher != null && teacher.getRole() == UsersRole.TEACHER) {
            course.setTeacherId(teacherId);
            courseRepository.save(course);
        }
        return "redirect:/courses";
    }


    @PostMapping("/course/{courseId}/addStudent/{studentId}")
    public String addStudentToCourse(@PathVariable String courseId, @PathVariable String studentId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        User student = userRepository.findById(studentId).orElse(null);

        if (course != null && student != null && course.isApproved()) {
            if (!course.getEnrolledStudents().contains(studentId)) {
                course.getEnrolledStudents().add(studentId);
                course.setCurrentCapacity(course.getCurrentCapacity() + 1);
                courseRepository.save(course);
            }
        }
        return "redirect:/courses";
    }


    @PostMapping("/course/complete/{courseId}")
    public String completeCourse(@PathVariable String courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null && !course.isCompleted()) {
            course.setCompleted(true);
            courseRepository.save(course);


            for (String studentId : course.getEnrolledStudents()) {
                User student = userRepository.findById(studentId).orElse(null);
                if (student != null) {
                    
                    if (course.getLevel().equalsIgnoreCase("Intermediate") && student.getProficiencyLevel().equalsIgnoreCase("Basic")) {
                        student.setProficiencyLevel("Intermediate");
                    } else if (course.getLevel().equalsIgnoreCase("Advanced") && student.getProficiencyLevel().equalsIgnoreCase("Intermediate")) {
                        student.setProficiencyLevel("Advanced");
                    }
                    userRepository.save(student);
                }
            }
        }
        return "redirect:/courses";
    }
}
