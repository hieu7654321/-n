package dev.hieu.mcenterproject.repository;

import dev.hieu.mcenterproject.model.ContactForm;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactFormRepository extends MongoRepository<ContactForm, String> {
    List<ContactForm> findAllByStatus(String status);
}
