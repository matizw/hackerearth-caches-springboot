package com.example.caching;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends CrudRepository<Book, UUID> {

  Optional<Book> findFirstByTitle(String title);


}