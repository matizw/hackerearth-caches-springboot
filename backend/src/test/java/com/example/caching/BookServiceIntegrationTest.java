package com.example.caching;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@EntityScan(basePackageClasses = Book.class)
@SpringBootTest(classes = DemoApplication.class)
@EnableJpaRepositories(basePackageClasses = BookRepository.class)
@ComponentScan(basePackages = {"com.example.caching"})
public class BookServiceIntegrationTest {
  @Autowired
  CacheManager cacheManager;

  @Autowired
  BookService service;

  private static final UUID I_ROBOT_ID = UUID.randomUUID();
  private static final UUID FOUNDATION_ID = UUID.randomUUID();
  private static final UUID DUNE_ID = UUID.randomUUID();

  private static final String I_ROBOT_ISBN = "ROBOT-ISBN";
  private static final String FOUNDATION_ISBN = "FOUNDATION-ISBN";
  private static final String DUNE_ISBN = "DUNE-ISBN";

  @BeforeEach
  void setUp() {
    service.save(new Book(DUNE_ID, "Dune", DUNE_ISBN));
    service.save(new Book(I_ROBOT_ID, "I, Robot", I_ROBOT_ISBN));
    service.save(new Book(FOUNDATION_ID, "Foundation", FOUNDATION_ISBN));
  }

  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBePutInCache() {
    Optional<Book> dune = service.findFirstByTitle("Dune");
    Optional<Book> foundation = service.findFirstByTitle("Foundation");
    Optional<Book> robot = service.findFirstByTitle("I, Robot");
    assertEquals(dune, getCachedBook("Dune"));
    assertEquals(foundation, getCachedBook("Foundation"));
    assertEquals(robot, getCachedBook("I, Robot"));
  }


  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBeUpdatedInCache() {
    Optional<Book> dune = service.findFirstByTitle("Dune");
    Optional<Book> foundation = service.findFirstByTitle("Foundation");
    Optional<Book> robot = service.findFirstByTitle("I, Robot");
    assertEquals(robot, getCachedBook("I, Robot") );
    Book updated = service.update(new Book(I_ROBOT_ID, "I, Robot", "NEW-ISBN"));
    assertEquals(dune, getCachedBook("Dune"));
    assertEquals(foundation, getCachedBook("Foundation"));
    assertEquals(Optional.of(updated), getCachedBook("I, Robot") );
  }

  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBeEvictedInCache() {
    Optional<Book> dune = service.findFirstByTitle("Dune");
    Optional<Book> robot = service.findFirstByTitle("I, Robot");
    Optional<Book> foundation = service.findFirstByTitle("Foundation");

    service.delete(new Book(DUNE_ID, "Dune", DUNE_ISBN));

    assertEquals(empty(), getCachedBook("Dune"));
    assertEquals(robot, getCachedBook("I, Robot"));
    assertEquals(foundation, getCachedBook("Foundation"));
  }


  private Optional<Book> getCachedBook(String title) {
    return ofNullable(cacheManager.getCache("books")).map(c -> c.get(title, Book.class));
  }

  @AfterEach
  void clear() {
    cacheManager.getCache("books").clear();
  }
}
