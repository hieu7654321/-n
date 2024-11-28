package dev.hieu.mcenterproject.controller;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.*;
import dev.hieu.mcenterproject.repository.*;
import dev.hieu.mcenterproject.service.EmailService;
import dev.hieu.mcenterproject.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final TienghathocvienRepository tienghathocvienRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final NewsRepository newsRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Your username and password are invalid.");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    // Hiển thị form đăng ký
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    @GetMapping("/loginSuccess")
    public String loginSuccessPage() {
        return "loginSuccess"; // Trả về trang loginSuccess.html
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Lấy tên đăng nhập của người dùng và thêm vào model
            model.addAttribute("username", authentication.getName());
        }
        return "home"; // Tên file HTML cho trang home
    }


    // Xử lý đăng ký người dùng
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        try {
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "Username already exists.");
                return "register";
            }

            // Đặt vai trò mặc định và trạng thái chưa active
            user.setRole(UsersRole.STUDENT);
            user.setActive(false);

            // Mã hóa mật khẩu trước khi lưu
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Tạo mã xác nhận
            String verificationCode = UUID.randomUUID().toString();
            user.setVerificationCode(verificationCode);
            // Thiết lập thời gian hết hạn
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));

            // Lưu user vào database
            userRepository.save(user);

            // Gửi email xác nhận
            String subject = "Please confirm your account, this link will expire in 15 minutes";
            String confirmationUrl = "http://localhost:8080/confirm-email?code=" + verificationCode;
            String message = "Click the link below to verify your email:\n" + confirmationUrl;
            emailService.sendEmail(user.getEmail(), subject, message);

            // Chuyển hướng tới trang xác nhận email thay vì trang đăng nhập
            model.addAttribute("message", "Registration successful! Please check your email to confirm your account.");
            return "emailConfirmation"; // Đây sẽ là trang thông báo yêu cầu xác nhận email
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@RequestParam("code") String code, Model model) {
        System.out.println("Received verification code: " + code);

        try {
            User user = userRepository.findByVerificationCode(code);

            if (user == null) {
                model.addAttribute("error", "Invalid verification code.");
                return "redirect:/error";
            }

            // Kiểm tra xem mã xác thực có còn hiệu lực không
            if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "Verification code has expired. Please register again.");
                return "redirect:/error";
            }

            // Kiểm tra xem tài khoản đã được kích hoạt chưa
            if (user.isActive()) {
                model.addAttribute("message", "This email has already been confirmed. You can now login.");
                return "confirmsuccess"; // Hoặc điều hướng đến trang khác nếu bạn muốn
            }

            // Kích hoạt tài khoản và cập nhật thông tin người dùng
            user.setActive(true);
            user.setVerificationCode(null); // Xóa mã xác nhận
            user.setVerificationExpiration(null); // Xóa thời gian hết hạn

            // Lưu người dùng vào cơ sở dữ liệu
            userRepository.save(user);

            // Thêm thông báo thành công vào mô hình
            model.addAttribute("message", "Email confirmed successfully! You can now login.");

            return "confirmsuccess";

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while confirming your email. Please try again.");
            logger.error("Error confirming email for code: {}", code, e);
            return "redirect:/error";
        }
    }
    @PostMapping("/login")
    public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
        // Kiểm tra thông tin đăng nhập (có thể dùng AuthenticationManager để xác thực)
        Authentication authentication = userService.authenticate(username, password);
        if (authentication != null) {
            // Lưu thông tin người dùng vào session
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("user", userDetails);
            return "redirect:/home"; // Chuyển hướng đến trang home
        } else {
            model.addAttribute("error", "Invalid username or password.");
            return "login"; // Quay lại trang đăng nhập nếu thất bại
        }
    }

    @GetMapping("/logout")
    public String logout() {
        // Xóa thông tin người dùng khỏi session
        SecurityContextHolder.clearContext();
        return "redirect:/login"; // Chuyển hướng về trang đăng nhập
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/login"; // Redirect to login if user not found
        }

        String role = user.getRole().name();
        model.addAttribute("role", role);
        model.addAttribute("user", user);
        return "profile"; // Return profile.html
    }


    @PostMapping("/profile/update")
    public String updateProfile(User updatedUser, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "profile";
        }


        user.setAvatar(updatedUser.getAvatar());
        user.setSurname(updatedUser.getSurname());
        user.setLastname(updatedUser.getLastname());
        user.setDob(updatedUser.getDob());
        user.setGender(updatedUser.getGender());
        user.setAddress(updatedUser.getAddress());
        user.setPhone(updatedUser.getPhone());

        userRepository.save(user);

        model.addAttribute("user", user);
        model.addAttribute("success", "Profile updated successfully.");
        return "redirect:/profile";
    }

    // Hiển thị danh sách tin tức
    @GetMapping("/event")
    public String showNewsList(Model model, Authentication authentication) {
        // Kiểm tra xem người dùng đã đăng nhập chưa và lấy thông tin role
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                String role = user.getRole().name();
                model.addAttribute("role", role);
            }
        }

        // Lấy danh sách tin tức đã được sắp xếp theo publishDate giảm dần
        List<News> newsList = newsRepository.findAll(Sort.by(Sort.Direction.DESC, "publishDate")).stream()
                .map(news -> {
                    // Tạo preview cho nội dung
                    String contentPreview = news.getContent();
                    if (contentPreview.length() > 200) {
                        contentPreview = contentPreview.substring(0, 200) + "...";
                    }
                    news.setContent(contentPreview);  // Cập nhật nội dung với preview
                    return news;
                })
                .collect(Collectors.toList());

        // Thêm danh sách tin tức vào model để hiển thị trên view
        model.addAttribute("newsList", newsList);

        return "newsList";  // Trả về trang Thymeleaf để hiển thị danh sách tin tức
    }


    @GetMapping("/event/{id}")
    public String showNewsDetail(@PathVariable String id, Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        News news = newsRepository.findById(id).orElse(null);
        if (news == null) {
            return "redirect:/event";
        }

        model.addAttribute("news", news);
        return "newsDetail";
    }

    @GetMapping("/schedule")
    public String viewSchedule(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (user != null && authentication.isAuthenticated()) {
            model.addAttribute("user", user);
        }

        Map<String, Map<String, List<Course>>> schedule = new LinkedHashMap<>();
        for (String day : List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")) {
            schedule.put(day, new LinkedHashMap<>());
            schedule.get(day).put("morning", new ArrayList<>());
            schedule.get(day).put("afternoon", new ArrayList<>());
            schedule.get(day).put("evening", new ArrayList<>());
        }

        if (user != null) {
            List<Course> courses = new ArrayList<>();

            if (user.getRole() == UsersRole.STUDENT) {
                courses = courseRepository.findByEnrolledStudentsContainingAndApprovedAndCompleted(user.getId(), true, false);
            } else if (user.getRole() == UsersRole.TEACHER) {
                courses = courseRepository.findByTeacherIdAndApprovedAndCompleted(user.getId(), true, false);
            }

            for (Course course : courses) {
                String[] timeParts = course.getSchedule().split(",")[0].trim().split(" - ");
                String startTime = timeParts[0];
                String courseDay = course.getSchedule().split(",")[1].trim();

                String dayOfWeek = getDayOfWeek(courseDay);

                String timeOfDay = getTimeOfDay(startTime);

                if (schedule.containsKey(dayOfWeek) && schedule.get(dayOfWeek).containsKey(timeOfDay)) {
                    schedule.get(dayOfWeek).get(timeOfDay).add(course);
                }
            }
        }

        model.addAttribute("schedule", schedule);

        return "classSchedule";
    }


    private String getTimeOfDay(String startTime) {
        int hour = Integer.parseInt(startTime.split(":")[0]);
        if (hour >= 6 && hour < 12) {
            return "morning";
        } else if (hour >= 12 && hour < 18) {
            return "afternoon";
        } else {
            return "evening";
        }
    }

    public String getDayOfWeek(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            switch (dayOfWeek) {
                case Calendar.MONDAY: return "Monday";
                case Calendar.TUESDAY: return "Tuesday";
                case Calendar.WEDNESDAY: return "Wednesday";
                case Calendar.THURSDAY: return "Thursday";
                case Calendar.FRIDAY: return "Friday";
                case Calendar.SATURDAY: return "Saturday";
                case Calendar.SUNDAY: return "Sunday";
                default: return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    @GetMapping("/tieng-hat-hoc-vien")
    public String showTiengHatHocVien(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // Lấy tên đăng nhập của người dùng và thêm vào model
            model.addAttribute("username", authentication.getName());
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            String role = user.getRole().name();
            model.addAttribute("role", role);
        }
        List<Tienghathocvien> danhsach = tienghathocvienRepository.findAll(Sort.by(Sort.Direction.DESC, "thoigiandang"));

        model.addAttribute("danhsach", danhsach);
        return "tiengHatHocVien";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tieng-hat-hoc-vien/upload")
    public String uploatTiengHatHocVien(@RequestParam("link") String link,
                                        @RequestParam("name") String name,
                                        Model model, Authentication authentication) {

        Tienghathocvien tienghathocvien1 = Tienghathocvien.builder()
                .name(name)
                .link(link)
                .thoigiandang(LocalDateTime.now())
                .build();
        tienghathocvienRepository.save(tienghathocvien1);


        return "redirect:/tieng-hat-hoc-vien";
    }

    @GetMapping("/about")
    public String about(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }
        return "about";
    }

    @GetMapping("/hocphi")
    public String viewTuitionFees(Model model, Authentication authentication) {
        // Lấy thông tin người dùng
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (user != null && authentication.isAuthenticated()) {
            // Thêm thông tin người dùng vào model
            model.addAttribute("user", user);

            // Lấy danh sách các khóa học đã đăng ký
            List<Course> courses = courseRepository.findByEnrolledStudentsContaining(user.getId());

            // Tính tổng học phí và số tiền còn nợ
            int totalFee = 0;
            int totalPaid = 0;

            for (Course course : courses) {
                totalFee += course.getFee();
                totalPaid += course.getAmountPaid();
            }

            int totalOutstanding = totalFee - totalPaid;

            // Thêm dữ liệu vào model
            model.addAttribute("courses", courses);
            model.addAttribute("totalFee", totalFee);
            model.addAttribute("totalPaid", totalPaid);
            model.addAttribute("totalOutstanding", totalOutstanding);
        }

        return "tuitionFees";
    }


//    @GetMapping("/test-schedule")
//    public String testSchedule(Model model, Authentication authentication) {
//        // Lấy thông tin người dùng từ Authentication
//        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
//
//        if (user != null) {
//            List<Course> courses = new ArrayList<>();
//
//            if (user.getRole() == UsersRole.STUDENT) {
//                // Nếu là STUDENT, tìm khóa học dựa trên enrolledStudents
//                courses = courseRepository.findByEnrolledStudentsContainingAndApprovedAndCompleted(user.getId(), true, false);
//            } else if (user.getRole() == UsersRole.TEACHER) {
//                // Nếu là TEACHER, tìm khóa học dựa trên teacherId
//                courses = courseRepository.findByTeacherIdAndApprovedAndCompleted(user.getId(), true, false);
//            }
//
//            // Khởi tạo cấu trúc thời khóa biểu (không cần chia cột, chỉ in ra thông tin)
//            Map<String, Map<String, List<Course>>> schedule = new LinkedHashMap<>();
//            for (String day : List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")) {
//                schedule.put(day, new LinkedHashMap<>());
//                schedule.get(day).put("morning", new ArrayList<>());
//                schedule.get(day).put("afternoon", new ArrayList<>());
//                schedule.get(day).put("evening", new ArrayList<>());
//            }
//
//            // Xử lý dữ liệu khóa học để phân loại vào thời gian trong ngày
//            for (Course course : courses) {
//                System.out.println("Course Schedule: " + course.getSchedule());  // Debug thông tin lịch học
//
//                String[] timeParts = course.getSchedule().split(",")[0].trim().split(" - ");
//                String startTime = timeParts[0];
//                String courseDay = course.getSchedule().split(",")[1].trim();
//
//                // Convert the course day to a day of the week
//                String dayOfWeek = getDayOfWeek(courseDay);
//
//                // Xác định thời gian trong ngày (sáng, chiều, tối)
//                String timeOfDay = getTimeOfDay(startTime);
//                System.out.println("Parsed Day: " + dayOfWeek + ", Time of Day: " + timeOfDay);  // Debug các giá trị đã phân tích
//
//                // Thêm vào cấu trúc thời khóa biểu
//                if (schedule.containsKey(dayOfWeek) && schedule.get(dayOfWeek).containsKey(timeOfDay)) {
//                    schedule.get(dayOfWeek).get(timeOfDay).add(course);
//                } else {
//                    System.out.println("No matching slot for " + dayOfWeek + " at " + timeOfDay);  // Debug trường hợp không tìm thấy
//                }
//            }
//
//
//
//            // In ra thông tin ngày và thời gian của mỗi khóa học
//            for (String day : schedule.keySet()) {
//                for (String timeOfDay : schedule.get(day).keySet()) {
//                    List<Course> courseList = schedule.get(day).get(timeOfDay);
//                    for (Course course : courseList) {
//                        // In ra thông tin ngày và thời gian
//                        System.out.println("Day: " + day + ", Time: " + timeOfDay + ", Course: " + course.getName());
//                    }
//                }
//            }
//
//            // Thêm thời khóa biểu vào model (nếu cần hiển thị trong view)
//            model.addAttribute("schedule", schedule);
//        }
//
//        return "testResult"; // Tên view hiển thị kết quả
//    }




//    @GetMapping("/history")
//    public String viewHistory(Model model, Authentication authentication) {
//        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
//
//        if (user != null) {
//            List<Course> completedCourses = courseRepository.findCompletedCoursesByStudent(user.getId());
//            model.addAttribute("completedCourses", completedCourses);
//        }
//        return "classHistory"; // Render history.html
//    }
}
