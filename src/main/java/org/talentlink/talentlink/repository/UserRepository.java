package org.talentlink.talentlink.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.talentlink.talentlink.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleId IS NULL OR u.roleId = :roleId)
              AND (:isActive IS NULL OR u.isActive = :isActive)
            ORDER BY u.id DESC
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}