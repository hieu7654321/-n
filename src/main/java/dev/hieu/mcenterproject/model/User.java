package dev.hieu.mcenterproject.model;

import dev.hieu.mcenterproject.Eum.UsersRole;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")

public class User {

    @MongoId
    private String id;
    @Indexed
    private String username;
    private String password;
    private boolean active;
    private boolean locked;
    private String lastname;
    private String avatar;
    private String Dob;
    private String Phone;
    private String email;
    private UsersRole role; // Role: ADMIN, STUDENT, TEACHER
    private List<String> enrolledCourses = new ArrayList<>();
    private List<String> courses;
    private String surname;
    private String gender;
    private String address;
    private String description;
    private LocalDateTime verificationExpiration;
    private String verificationCode;
    private String ProficiencyLevel;
    private List<Notification> notifications = new ArrayList<>();
}
