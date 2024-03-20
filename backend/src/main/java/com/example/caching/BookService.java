package com.example.caching;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;

  @Cacheable(value = "books")
  public Optional<Book> findFirstByTitle(String title) {
    return bookRepository.findFirstByTitle(title);
  }

  public Book save(Book book) {
    return bookRepository.save(book);
  }

  @CachePut(key = "#book.getId", cacheNames = {"books"})
  public Book update(Book book) {
    Book bookDb = bookRepository.findById(book.getId()).orElseThrow(
        () -> new EntityNotFoundException("Book with id: " + book.getId() + ", not available."));

    bookDb.setTitle(book.getTitle());
    return bookRepository.save(bookDb);
  }


  @CacheEvict(value = "books", key="#book.title")
  public void delete(Book book) {
    bookRepository.delete(book);
  }

}
