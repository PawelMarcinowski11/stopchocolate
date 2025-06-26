package business.marcinowski.stopchocolate.auth.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import business.marcinowski.stopchocolate.auth.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByHashedTokenAndExpiryDateAfter(byte[] hashedToken, Instant expiryDate);

    void deleteByHashedToken(byte[] hashedToken);
}
