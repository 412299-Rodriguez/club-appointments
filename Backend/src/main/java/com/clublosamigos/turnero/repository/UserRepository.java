package com.clublosamigos.turnero.repository;

import com.clublosamigos.turnero.model.User;
import com.clublosamigos.turnero.model.User.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);

    boolean existsByEmail(String email);

    List<User> findByIsDeletedFalse();

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    List<User> findByRoleAndIsDeletedFalse(UserRole role);
}
