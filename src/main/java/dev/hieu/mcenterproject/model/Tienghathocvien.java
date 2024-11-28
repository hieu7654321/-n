package dev.hieu.mcenterproject.model;


import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.lang.annotation.Inherited;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Tienghathocvien")

public class Tienghathocvien {
    @MongoId
    private String id;
    @Indexed
    private String name;
    private String link;
    private LocalDateTime thoigiandang;
}
