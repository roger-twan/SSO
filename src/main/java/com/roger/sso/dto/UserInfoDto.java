package com.roger.sso.dto;

import java.util.List;

import com.roger.sso.entity.UserAuthedHost;
import com.roger.sso.entity.UserActivityLog;

public class UserInfoDto {
  private String email;
  private List<UserAuthedHost> authorizedHosts;
  private List<UserActivityLog> activityLogs;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<UserAuthedHost> getAuthorizedHosts() {
    return authorizedHosts;
  }

  public void setAuthorizedHosts(List<UserAuthedHost> authorizedHosts) {
    this.authorizedHosts = authorizedHosts;
  }

  public List<UserActivityLog> getActivityLogs() {
    return activityLogs;
  }

  public void setActivityLogs(List<UserActivityLog> activityLogs) {
    this.activityLogs = activityLogs;
  }
}
