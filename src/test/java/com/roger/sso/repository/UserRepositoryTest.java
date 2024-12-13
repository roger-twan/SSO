package com.roger.sso.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.roger.sso.entity.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  public void testUserCount() {
    Long count = userRepository.count();

    assertThat(count).isNotNull();
  }

  @Test
  public void testFindByEmailWithUserExists() {
    String testEmail = "test@example.com";
    User user = new User();
    user.setId("testId");
    user.setEmail(testEmail);
    user.setStatus(0);
    user.setPassword("testPassword");
    userRepository.save(user);

    Optional<User> result = userRepository.findByEmail(testEmail);

    assertTrue(result.isPresent());
    assertTrue(result.get().getEmail().equals(testEmail));
  }

  @Test
  public void testFindByEmailWithUserDoesNotExist() {
    Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

    assertFalse(result.isPresent());
  }

  @Test
  public void testDeleteByEmail() {
    String testEmail = "test@example.com";
    User user = new User();
    user.setId("testId");
    user.setEmail(testEmail);
    user.setStatus(0);
    user.setPassword("testPassword");
    userRepository.save(user);

    userRepository.deleteByEmail(testEmail);

    Optional<User> result = userRepository.findByEmail(testEmail);

    assertFalse(result.isPresent());
  }
}
