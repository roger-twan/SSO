package com.roger.sso.repository;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.roger.sso.entity.UserAuthedHost;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserAuthedHostRepositoryTest {
  @Autowired
  private UserAuthedHostRepository userAuthedHostRepository;

  @Test
  public void findUserAuthedHostWithExists() {
    UserAuthedHost testHost = new UserAuthedHost();
    testHost.setId("testId");
    testHost.setUserId("testUserId");
    testHost.setHost("testHost");
    userAuthedHostRepository.save(testHost);

    Optional<UserAuthedHost> result = userAuthedHostRepository.findUserAuthedHost("testUserId", "testHost");

    assertThat(result).isPresent();
    assertThat(result.get().getUserId()).isEqualTo("testUserId");
    assertThat(result.get().getHost()).isEqualTo("testHost");
  }

  @Test
  public void findUserAuthedHostWithNotExist() {
    Optional<UserAuthedHost> result = userAuthedHostRepository.findUserAuthedHost("nonExistentUser", "nonExistentHost");

    assertThat(result).isEmpty();
  }
}
