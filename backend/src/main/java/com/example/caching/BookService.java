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

  // Write the correct annotation to Cache the results from findFirstByTitle with the name "books".
  @Cacheable("books")
  public Optional<Book> findFirstByTitle(String title) {
    return bookRepository.findFirstByTitle(title);
  }

  //Write the correct annotation specifies that the result of the update method will be stored in the "books" cache with
  //the provided book title as the key, in order to guarantee that the cache is always refreshed with the latest book data.
  @CachePut(value = "books", key="#book.title")
  public Book update(Book book) {
    Book bookDb = bookRepository.findById(book.getId()).orElseThrow(
        () -> new EntityNotFoundException("Book with id: " + book.getId() + ", not available."));

    bookDb.setTitle(book.getTitle());
    bookDb.setIsbn(book.getIsbn());
    return bookRepository.save(bookDb);
  }

  // Write the correct annotation to remove the deleted book with the specified title from the "books" cache with
  // the provided book title as the key, in order to ensure that the cache is updated after a book is deleted.
  @CacheEvict(value = "books", key="#book.title")
  public void delete(Book book) {
    bookRepository.delete(book);
  }

  //Do NOT edit this method
  public Book save(Book book) {
    return bookRepository.save(book);
  }

}
