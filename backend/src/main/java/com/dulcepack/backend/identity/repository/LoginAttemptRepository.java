package com.dulcepack.backend.identity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dulcepack.backend.identity.domain.LoginAttemptEntity;

import jakarta.persistence.LockModeType;

public interface LoginAttemptRepository extends JpaRepository<LoginAttemptEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from LoginAttemptEntity attempt where attempt.identifier = :identifier")
    Optional<LoginAttemptEntity> findForUpdate(@Param("identifier") String identifier);
}
