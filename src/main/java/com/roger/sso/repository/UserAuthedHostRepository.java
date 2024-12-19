package com.roger.sso.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.roger.sso.entity.UserAuthedHost;

import jakarta.transaction.Transactional;

public interface UserAuthedHostRepository extends JpaRepository<UserAuthedHost, String> {
  @Transactional
  public void deleteByUserId(String email);

  @Query("SELECT uah FROM UserAuthedHost uah WHERE uah.userId = :userId AND uah.host = :host")
  public Optional<UserAuthedHost> findUserAuthedHost(@Param("userId") String userId, @Param("host") String host);

  @Query("SELECT uah FROM UserAuthedHost uah WHERE uah.userId = :userId")
  public List<UserAuthedHost> findUserAuthedHosts(@Param("userId") String userId);
}
