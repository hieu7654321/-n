package dev.hieu.mcenterproject.controller;

import dev.hieu.mcenterproject.model.ContactForm;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.ContactFormRepository;
import dev.hieu.mcenterproject.repository.UserRepository;
import dev.hieu.mcenterproject.service.ContactFormService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class ContactController {
    private final ContactFormService contactFormService;
    private final ContactFormRepository contactFormRepository;
    private final UserRepository userRepository;

    @GetMapping("/contact")
    public String showContactForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()){
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            String role = user.getRole().name();
            model.addAttribute("role", role);
            model.addAttribute("user", user);
            model.addAttribute("username", authentication.getName());
            return "contactForm";
        }
        return "redirect:/login";
    }


    @PostMapping("/contact/submit")
    public String submitContactForm(@RequestParam String title, @RequestParam String content, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ("Đăng ký lớp 1 - 1".equalsIgnoreCase(title)) {
            content = "Nội dung của tôi sẽ nghĩ sau";
        }

        contactFormService.createContactForm(user.getId(), title, content);
        return "redirect:/contact";
    }

    @GetMapping("/admin/contacts")
    public String showAdminContacts(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        List<ContactForm> pendingContacts = contactFormRepository.findAllByStatus("Pending");
        model.addAttribute("contacts", pendingContacts);
        return "adminContacts";
    }

    @PostMapping("/admin/contact/approve/{formId}")
    public String approveContactForm(@PathVariable String formId) {
        contactFormService.approveContactForm(formId);
        return "redirect:/admin/contacts";
    }

    @PostMapping("/admin/contact/reject/{formId}")
    public String rejectContactForm(@PathVariable String formId) {
        contactFormService.rejectContactForm(formId);
        return "redirect:/admin/contacts";
    }
}
