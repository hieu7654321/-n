package dev.hieu.mcenterproject.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "news")
public class News {
    @MongoId
    private String id;
    private String title;
    private String content;
    private String author;
    private LocalDate publishDate; // Sử dụng LocalDate để lưu ngày đăng
}
