package com.roger.sso.service;

import com.roger.sso.entity.User;
import com.roger.sso.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User createUser(User user) {
    return userRepository.save(user);
  }
}
