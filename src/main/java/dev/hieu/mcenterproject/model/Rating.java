package dev.hieu.mcenterproject.model;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "rating")
public class Rating {
    @MongoId
    private String id;
    @Indexed
    private String teacherId;
    private String userId;
    private Integer rating;
}
