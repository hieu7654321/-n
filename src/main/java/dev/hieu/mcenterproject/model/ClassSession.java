package dev.hieu.mcenterproject.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "class_sessions")
public class ClassSession {
    @Id
    private String id;
    private String courseId;
    private String date;
    private String timeOfDay; // "morning", "afternoon", "evening"
    private String teacherId;
    private List<String> studentIds = new ArrayList<>();
    private String status; // "scheduled", "completed"
}
