package com.milkcheck.repository;

import com.milkcheck.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
           SELECT u FROM User u
           WHERE NOT EXISTS (SELECT r.id FROM u.roles r WHERE r.name = 'ROLE_ADMIN')
             AND (:keyword IS NULL
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<User> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE NOT EXISTS (SELECT r.id FROM u.roles r WHERE r.name = 'ROLE_ADMIN')")
    long countCustomers();

}