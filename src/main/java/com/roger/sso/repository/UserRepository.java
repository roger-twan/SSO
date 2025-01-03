package com.roger.sso.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roger.sso.entity.User;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);

  @Transactional
  public void deleteByEmail(String email);
}
