package com.rsimplicio.springblog.repository;

import com.rsimplicio.springblog.model.Autor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AutorRepository extends MongoRepository<Autor, String> {

}
