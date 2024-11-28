package dev.hieu.mcenterproject.controller;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.Course;
import dev.hieu.mcenterproject.model.Rating;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.CourseRepository;
import dev.hieu.mcenterproject.repository.RatingRepository;
import dev.hieu.mcenterproject.repository.UserRepository;
import dev.hieu.mcenterproject.service.RatingService;
import dev.hieu.mcenterproject.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class TeacherController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RatingService ratingService;
    private final RatingRepository ratingRepository;


    @GetMapping("/teacher/courses")
    public String showTeacherCourses(Model model, Authentication authentication) {
        String teacherId = authentication.getName();


        List<Course> teacherCourses = courseRepository.findAll()
                .stream()
                .filter(course -> teacherId.equals(course.getTeacherId()))
                .collect(Collectors.toList());

        model.addAttribute("course", teacherCourses);
        return "teacherCourses";
    }


    @GetMapping("/teacher/course/{courseId}/students")
    public String showCourseStudents(@PathVariable String courseId, Model model) {
        Course course = courseRepository.findById(courseId).orElse(null);

        if (course == null) {
            model.addAttribute("error", "Course not found.");
            return "teacherCourses";
        }


        List<User> students = userRepository.findAllById(course.getEnrolledStudents());
        model.addAttribute("course", course);
        model.addAttribute("students", students);

        return "courseStudents";
    }

    @GetMapping("/teacher")
    public String showTeacher(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        List<User> teachers = userRepository.findAllByRole(UsersRole.TEACHER);
        model.addAttribute("users", user);
        model.addAttribute("teachers", teachers);
        return "teacher";
    }

//    @GetMapping("/{teacherId}/details")
//    public String showTeacherDetails(@PathVariable String teacherId, Model model) {
//        User teacher = userRepository.findById(teacherId).orElse(null);
//
//        if (teacher == null || teacher.getRole() != UsersRole.TEACHER) {
//            model.addAttribute("error", "Teacher not found.");
//            return "redirect:/teacher";
//        }
//
//        model.addAttribute("teacher", teacher);
//        model.addAttribute("averageRating", userService.calculateAverageRating(teacherId));
//        model.addAttribute("reviewCount", userService.getReviewCount(teacherId));
//
//        return "teacherDetails";
//    }
//

    @PostMapping("/registerToTeach/{courseId}")
    public String registerToTeach(@PathVariable String courseId, Authentication authentication) {
        Course course = courseRepository.findById(courseId).orElse(null);
        User teacher = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (course != null && teacher != null && teacher.getRole() == UsersRole.TEACHER) {
            course.setTeacherId(teacher.getId());
            courseRepository.save(course);
        }
        return "redirect:/courses";
    }

    @PostMapping("/course/{courseId}/uploadMaterial")
    public String uploadMaterial(
            @PathVariable String courseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) {
        // Logic to save file and description into course materials
        // Example: Save file to a directory or cloud storage, and store metadata in MongoDB
        return "redirect:/teacher/course/" + courseId;
    }


    // Post assignments
    @PostMapping("/course/{courseId}/postAssignment")
    public String postAssignment(
            @PathVariable String courseId,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("deadline") String deadline) {
        // Logic to create and save assignment into the course
        // Example: Save to a MongoDB collection for assignments
        return "redirect:/teacher/course/" + courseId;
    }


    // Send message to students
    @PostMapping("/course/{courseId}/sendMessage")
    public String sendMessage(
            @PathVariable String courseId,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message) {
        // Logic to send message to all students in the course
        // Example: Store messages in a course-related "messages" collection or notify users directly
        return "redirect:/teacher/course/" + courseId;
    }

    @GetMapping("/giang-vien")
    public String showGiangVien(Model model, Authentication authentication) {
        UsersRole usersRole = UsersRole.TEACHER;
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        List<User> teachers = userRepository.findAllByRole(usersRole);
        model.addAttribute("teachers", teachers);
        return "giangvien";
    }

    @GetMapping("/giang-vien/{teacherId}")
    public String showGiangVien(@PathVariable String teacherId, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);

            if (currentUser == null){
                return "redirect:/login";
            }
            String role = String.valueOf(currentUser.getRole());
            model.addAttribute("role", role);
        }
        User user = userRepository.findById(teacherId).orElse(null);

        double averageRating = ratingService.calculateAverageRating(teacherId);
        double ratingtb = Double.parseDouble(String.format("%.2f", averageRating));
        int totalReviews = ratingService.countTotalReviews(teacherId);

        model.addAttribute("averageRating", ratingtb);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("user", user);
        return "giangvienchitiet";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/giang-vien/{teacherId}/editdescription")
    public String editDescription(@PathVariable String teacherId, @RequestParam("description") String description, Model model) {
        User user = userRepository.findById(teacherId).orElse(null);

        if (user == null){
            return "redirect:/giang-vien/{teacherId}";
        }

        user.setDescription(description);
        userRepository.save(user);

        model.addAttribute("user", user);
        return "redirect:/giang-vien/{teacherId}";
    }

    @PostMapping("/giang-vien/{teacherId}/rate")
    public String rateTeacher(@PathVariable String teacherId, @RequestParam("rating") int rating, Model model,Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {

            User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (currentUser == null){
                return "redirect:/giang-vien/{teacherId}";
            }
            User user = userRepository.findById(teacherId).orElse(null);
            List<Rating> rate = ratingRepository.findByUserIdAndTeacherId(currentUser.getId(), teacherId);

            if (!rate.isEmpty()){
                return "redirect:/giang-vien/{teacherId}";
            }

            Rating rating1 = Rating.builder()
                    .teacherId(teacherId)
                    .userId(currentUser.getId())
                    .rating(rating)
                    .build();

            ratingRepository.save(rating1);

            if (user == null){
                return "redirect:/giang-vien/{teacherId}";
            }

            model.addAttribute("user", user);
            return "redirect:/giang-vien/{teacherId}";
        }
        return "redirect:/login";
    }

}
