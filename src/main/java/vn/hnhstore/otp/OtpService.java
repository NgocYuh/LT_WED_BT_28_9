package vn.hnhstore.otp;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_SECONDS = 60;
    private final OtpTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final OtpMailSender mail;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public OtpService(OtpTokenRepository tokens, PasswordEncoder encoder, OtpMailSender mail, Clock clock) {
        this.tokens = tokens; this.encoder = encoder; this.mail = mail; this.clock = clock;
    }

    @Transactional
    public void send(String email, OtpPurpose purpose) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now(clock);
        tokens.findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(normalized, purpose)
                .ifPresent(previous -> {
                    if (now.isBefore(previous.getCreatedAt().plusSeconds(RESEND_SECONDS))) {
                        throw new IllegalArgumentException("Vui lòng đợi 60 giây trước khi gửi lại OTP");
                    }
                    previous.consume();
                });
        String code = "%06d".formatted(random.nextInt(1_000_000));
        tokens.save(new OtpToken(normalized, purpose, encoder.encode(code), now));
        mail.send(normalized, code, purpose);
    }

    @Transactional
    public boolean consume(String email, OtpPurpose purpose, String code) {
        if (email == null || code == null) return false;
        OtpToken token = tokens.findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(
                email.trim().toLowerCase(Locale.ROOT), purpose).orElse(null);
        if (token == null || token.isUsed() || token.getAttempts() >= MAX_ATTEMPTS
                || !LocalDateTime.now(clock).isBefore(token.getExpiresAt())) return false;
        if (!code.matches("[0-9]{6}") || !encoder.matches(code, token.getOtpHash())) {
            token.recordFailure();
            return false;
        }
        token.consume();
        return true;
    }

    @Configuration
    static class TimeConfig {
        @Bean Clock otpClock() { return Clock.systemUTC(); }
    }
}
