package dev.hieu.mcenterproject;

import dev.hieu.mcenterproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MongoDbTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/test")
    public List<User> testConnection() {
        return mongoTemplate.findAll(User.class, "users");
    }
}
