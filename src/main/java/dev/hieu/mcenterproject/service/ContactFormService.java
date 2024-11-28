package dev.hieu.mcenterproject.service;

import dev.hieu.mcenterproject.model.ContactForm;
import dev.hieu.mcenterproject.model.Notification;
import dev.hieu.mcenterproject.model.User;
import dev.hieu.mcenterproject.repository.ContactFormRepository;
import dev.hieu.mcenterproject.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ContactFormService {
    private final ContactFormRepository contactFormRepository;
    private final UserRepository userRepository;

    public ContactForm createContactForm(String userId, String title, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ContactForm contactForm = ContactForm.builder()
                .userId(userId)
                .username(user.getUsername())
                .title(title)
                .content(content)
                .status("Pending")
                .build();
        return contactFormRepository.save(contactForm);
    }

    public void approveContactForm(String formId) {
        ContactForm form = contactFormRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        form.setStatus("Approved");
        contactFormRepository.save(form);

        notifyUser(form.getUserId(), "Your contact request titled '" + form.getTitle() + "' has been approved.");
    }

    public void rejectContactForm(String formId) {
        ContactForm form = contactFormRepository.findById(formId).orElseThrow(() -> new IllegalArgumentException("Form not found"));
        form.setStatus("Rejected");
        contactFormRepository.save(form);

        notifyUser(form.getUserId(), "Your contact request titled '" + form.getTitle() + "' has been rejected.");
    }

    private void notifyUser(String userId, String message) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.getNotifications().add(new Notification(message, false));
        userRepository.save(user);
    }
}
