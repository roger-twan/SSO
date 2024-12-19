package com.roger.sso.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roger.sso.entity.UserEventLog;

public interface UserEventLogRepository extends JpaRepository<UserEventLog, Long> {
}
