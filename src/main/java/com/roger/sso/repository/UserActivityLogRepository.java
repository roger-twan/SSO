package com.roger.sso.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.roger.sso.entity.UserActivityLog;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, String> {
  @Query("select u from UserActivityLog u where u.userId = :userId")
  public List<UserActivityLog> findUserActivityLogs(@Param("userId") String userId);
}
