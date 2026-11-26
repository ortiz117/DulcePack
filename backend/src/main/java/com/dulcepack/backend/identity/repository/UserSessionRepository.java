package com.dulcepack.backend.identity.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import com.dulcepack.backend.identity.domain.UserSessionEntity;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<UserSessionEntity> findByAccessTokenHashAndRevokedAtIsNull(String accessTokenHash);

    @EntityGraph(attributePaths = "user")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSessionEntity> findByRefreshTokenHashAndRevokedAtIsNull(String refreshTokenHash);

    @Modifying
    @Query("update UserSessionEntity s set s.revokedAt = :now where s.user.id = :userId and s.revokedAt is null")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
