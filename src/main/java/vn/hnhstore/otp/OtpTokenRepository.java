package vn.hnhstore.otp;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpToken> findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);
}
