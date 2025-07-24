package es.oscasais.pa.userService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.oscasais.pa.userService.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);

  boolean existsByEmailAndIdNot(String email, Long id);
}
