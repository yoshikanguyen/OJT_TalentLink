package org.talentlink.talentlink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.talentlink.talentlink.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);
}
