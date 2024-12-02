package com.roger.sso.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roger.sso.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {}
