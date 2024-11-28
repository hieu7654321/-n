package dev.hieu.mcenterproject.repository;

import dev.hieu.mcenterproject.model.Tienghathocvien;
import jdk.dynalink.linker.LinkerServices;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TienghathocvienRepository extends MongoRepository<Tienghathocvien, String> {
    List<Tienghathocvien> findByName(String name);
}
