package com.roger.sso.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    private int id;
    private String name;
    private String email;

    public int getId() {
      return id;
    }
    public String getName() {
      return name;
    }
    public String getEmail() {
      return email;
    }
    public void setName(String name) {
      this.name = name;
    }
    public void setEmail(String email) {
      this.email = email;
    }
}
