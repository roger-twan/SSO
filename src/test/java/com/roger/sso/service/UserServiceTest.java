package com.roger.sso.service;

import com.roger.sso.entity.User;
import com.roger.sso.repository.UserRepository;
import com.roger.sso.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testGetUserByEmail() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        User foundUser = userService.getUserByEmail("john.doe@example.com");

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("john.doe@example.com");
    }
}
