package com.example.caching;

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

  @BeforeEach
  void setUp() {
    service.save(new Book(DUNE_ID, "Dune"));
    service.save(new Book(I_ROBOT_ID, "I, Robot"));
    service.save(new Book(FOUNDATION_ID, "Foundation"));
  }

  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBePutInCache() {
    Optional<Book> dune = service.findFirstByTitle("Dune");

    assertEquals(dune, getCachedBook("Dune"));
  }

  @Test
  void givenBookThatShouldNotBeCached_whenFindByTitle_thenResultShouldNotBePutInCache() {
    service.findFirstByTitle("Foundation");

    assertEquals(empty(), getCachedBook("Foundation"));
  }

  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBePutInCache2() {
    Optional<Book> robot = service.findFirstByTitle("I, Robot");

    assertEquals(robot, getCachedBook("I, Robot"));
  }


  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBeUpdateInCache2() {
    service.update(new Book(I_ROBOT_ID, "I, Robot 2"));

    Optional<Book> dune = service.findFirstByTitle("Dune");
    Optional<Book> robot = service.findFirstByTitle("I, Robot");
    Optional<Book> found = service.findFirstByTitle("Foundation");
    Optional<Book> robot2 = service.findFirstByTitle("I, Robot 2");


    assertEquals(dune, getCachedBook("Dune"));
    assertEquals(empty(), getCachedBook("I, Robot"));
    assertEquals(empty(), getCachedBook("Foundation"));
    assertEquals(robot2, getCachedBook("I, Robot 2") );
  }

  @Test
  void givenBookThatShouldBeCached_whenFindByTitle_thenResultShouldBeDeleteInCache2() {
    Optional<Book> dune = service.findFirstByTitle("Dune");
    Optional<Book> robot = service.findFirstByTitle("I, Robot");
    Optional<Book> found = service.findFirstByTitle("Foundation");

    service.delete(new Book(DUNE_ID, "Dune"));

    assertEquals(empty(), getCachedBook("Dune"));
    assertEquals(robot, getCachedBook("I, Robot"));
    assertEquals(empty(), getCachedBook("Foundation"));
  }


  private Optional<Book> getCachedBook(String title) {
    return ofNullable(cacheManager.getCache("books")).map(c -> c.get(title, Book.class));
  }
}
