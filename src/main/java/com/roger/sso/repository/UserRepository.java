package com.roger.sso.repository;

import com.roger.sso.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  @Override
  default List<User> findAll() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'findAll'");
  }

  @Override
  default <S extends User> S save(S entity) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'save'");
  }

  default User getUserByEmail(String email) {
    // TODO Auto-generated method stub
    User user = new User();
    return user;
  }
}
