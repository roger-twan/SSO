package com.roger.sso.repository;

import com.roger.sso.entity.User;
import com.roger.sso.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveUser() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    public void testFindByEmail() {
        User user = new User();
        user.setName("Jane Doe");
        user.setEmail("jane.doe@example.com");

        userRepository.save(user);

        User foundUser = userRepository.getUserByEmail("jane.doe@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("jane.doe@example.com");
    }
}
