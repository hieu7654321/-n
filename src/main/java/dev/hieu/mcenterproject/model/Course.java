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
@Document(collection = "courses")
public class Course {
    @Id
    private String id;
    private String name;
    private String room;
    private int maxCapacity;
    private int currentCapacity;
    private int capacity;
    private List<String> students;
    private int fee;
    private int amountPaid;
    private int OutstandingFee;
    private String schedule; // Example: "9 AM - 11 AM, 14/10/2024"
    private String level; // Basic, Intermediate, Advanced
    private String teacherId; // ID of the assigned teacher
    private List<String> enrolledStudents = new ArrayList<>(); // User IDs of students
    private boolean approved = false;
    private boolean completed = false;

    public double getOutstandingFee() {
        return fee - amountPaid; // Tính số tiền còn nợ
    }
}
