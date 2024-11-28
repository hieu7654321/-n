package dev.hieu.mcenterproject.controller;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.Course;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.CourseRepository;
import dev.hieu.mcenterproject.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @GetMapping("/courses")
    public String showCourses(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        List<Course> courses = courseRepository.findAll();


        List<Course> enrolledCourses = courseRepository.findAllById(user.getEnrolledCourses());

        String role = user.getRole().name();
        model.addAttribute("role", role);
        model.addAttribute("courses", courses);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("user", user);
        return "courseRegistration";
    }


    @PostMapping("/courses/register/{courseId}")
    public String registerCourse(@PathVariable String courseId, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/courses";
        }

        if (course == null) {
            model.addAttribute("error", "Không tìm thấy lớp");
            return "redirect:/courses";
        }


        if (course.getEnrolledStudents().contains(user.getId())) {
            model.addAttribute("error", "Bạn đã ở trong lớp này rồi");
            return "redirect:/courses";
        }

        if (course.getCurrentCapacity() >= course.getMaxCapacity()) {
            model.addAttribute("error", "Lớp đã đầy.");
            return "redirect:/courses";
        }


        course.getEnrolledStudents().add(user.getId());
        course.setCurrentCapacity(course.getCurrentCapacity() + 1);
        courseRepository.save(course);


        user.getEnrolledCourses().add(course.getId());
        userRepository.save(user);

        model.addAttribute("success", "Đăng ký lớp thành công");
        return "redirect:/courses";
    }

    @PostMapping("/courses/cancel/{courseId}")
    public String cancelCourse(@PathVariable String courseId, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (user == null || course == null) {
            model.addAttribute("error", "User or Course not found.");
            return "courseRegistration";
        }


        if (!course.getEnrolledStudents().contains(user.getId())) {
            model.addAttribute("error", "You are not enrolled in this course.");
            return "courseRegistration";
        }


        course.getEnrolledStudents().remove(user.getId());
        course.setCurrentCapacity(course.getCurrentCapacity() - 1);
        courseRepository.save(course);


        user.getEnrolledCourses().remove(course.getId());
        userRepository.save(user);

        model.addAttribute("success", "Successfully canceled your registration for the course.");
        return "redirect:/courses";
    }

    @GetMapping("/lophoc")
    public String listUserCourses(Model model, Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (user == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Chuyển hướng nếu không đăng nhập
        }


        List<Course> userCourses = new ArrayList<>();
        if (user.getRole() == UsersRole.STUDENT) {
            userCourses = courseRepository.findByEnrolledStudentsContainingAndApprovedAndCompleted(user.getId(),true,false);
        } else if (user.getRole() == UsersRole.TEACHER) {
            userCourses = courseRepository.findByEnrolledStudentsContainingAndApprovedAndCompleted(user.getId(),true,false);
        }

        model.addAttribute("user", user);
        model.addAttribute("courses", userCourses);

        return "lophoc";
    }


    @GetMapping("/lophoc/{courseId}")
    public String viewCourseDetails(@PathVariable String courseId, Model model, Authentication authentication) {

        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (user == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }


        if (courseId == null || courseId.trim().isEmpty()) {
            return "redirect:/lophoc";
        }


        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return "redirect:/lophoc";
        }

        // Lấy tên đầy đủ của giáo viên
        String teacherName = "";
        if (course.getTeacherId() != null) {
            User teacher = userRepository.findById(course.getTeacherId()).orElse(null);
            if (teacher != null) {
                teacherName = teacher.getSurname() + " " + teacher.getLastname();
            }
        }

        // Tạo danh sách tên đầy đủ của các học viên
        List<String> studentFullNames = new ArrayList<>();
        for (String studentId : course.getEnrolledStudents()) {
            User student = userRepository.findById(studentId).orElse(null);
            if (student != null) {
                String fullName = student.getSurname() + " " + student.getLastname();
                studentFullNames.add(fullName);
            }
        }

        // Kiểm tra quyền truy cập
        boolean isAuthorized = course.getEnrolledStudents().contains(user.getId()) ||
                course.getTeacherId().equals(user.getId());

        if (!isAuthorized) {
            return "redirect:/lophoc"; // Nếu không có quyền, chuyển hướng về danh sách
        }

        model.addAttribute("teacherName", teacherName);
        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("studentFullNames", studentFullNames); // Thêm danh sách tên học viên vào model

        return "chitietlophoc";
    }



}
