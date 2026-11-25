package com.dulcepack.backend.identity.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dulcepack.backend.identity.domain.PasswordResetTokenEntity;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    @EntityGraph(attributePaths = "user")
    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedAtIsNull(String tokenHash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PasswordResetTokenEntity token
               set token.usedAt = :now
             where token.tokenHash = :tokenHash
               and token.usedAt is null
               and token.expiresAt > :now
            """)
    int consumeIfValid(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

    @Modifying
    @Query("update PasswordResetTokenEntity t set t.usedAt = :now where t.user.id = :userId and t.usedAt is null")
    int invalidateAllByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
