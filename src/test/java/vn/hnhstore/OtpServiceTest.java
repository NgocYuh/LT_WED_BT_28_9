package vn.hnhstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.hnhstore.otp.OtpMailSender;
import vn.hnhstore.otp.OtpPurpose;
import vn.hnhstore.otp.OtpService;
import vn.hnhstore.otp.OtpToken;
import vn.hnhstore.otp.OtpTokenRepository;

class OtpServiceTest {
    @Test void codeIsHashedRateLimitedExpiresAndCannotBeReused() {
        MutableClock clock = new MutableClock();
        AtomicReference<OtpToken> saved = new AtomicReference<>();
        OtpTokenRepository repo = mock(OtpTokenRepository.class);
        OtpMailSender mail = mock(OtpMailSender.class);
        when(repo.findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        when(repo.save(any())).thenAnswer(invocation -> {
            OtpToken token = invocation.getArgument(0);
            saved.set(token);
            return token;
        });
        OtpService service = new OtpService(repo, new BCryptPasswordEncoder(), mail, clock);
        service.send("TEST@example.test", OtpPurpose.REGISTER);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mail).send(eq("test@example.test"), code.capture(), eq(OtpPurpose.REGISTER));
        assertThat(code.getValue()).matches("[0-9]{6}");
        assertThat(saved.get().getOtpHash()).isNotEqualTo(code.getValue());
        assertThatThrownBy(() -> service.send("test@example.test", OtpPurpose.REGISTER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.consume("test@example.test", OtpPurpose.REGISTER, "not-a-code")).isFalse();
        assertThat(service.consume("test@example.test", OtpPurpose.REGISTER, code.getValue())).isTrue();
        assertThat(service.consume("test@example.test", OtpPurpose.REGISTER, code.getValue())).isFalse();
        clock.advanceSeconds(61);
        service.send("test@example.test", OtpPurpose.RESET);
        ArgumentCaptor<String> resetCode = ArgumentCaptor.forClass(String.class);
        verify(mail).send(eq("test@example.test"), resetCode.capture(), eq(OtpPurpose.RESET));
        clock.advanceSeconds(601);
        assertThat(service.consume("test@example.test", OtpPurpose.RESET, resetCode.getValue())).isFalse();
    }

    @Test void fiveFailuresLockTheCode() {
        MutableClock clock = new MutableClock();
        AtomicReference<OtpToken> saved = new AtomicReference<>();
        OtpTokenRepository repo = mock(OtpTokenRepository.class);
        OtpMailSender mail = mock(OtpMailSender.class);
        when(repo.findFirstByEmailIgnoreCaseAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        when(repo.save(any())).thenAnswer(invocation -> { saved.set(invocation.getArgument(0)); return saved.get(); });
        OtpService service = new OtpService(repo, new BCryptPasswordEncoder(), mail, clock);
        service.send("lock@example.test", OtpPurpose.REGISTER);
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mail).send(eq("lock@example.test"), code.capture(), eq(OtpPurpose.REGISTER));
        for (int i = 0; i < 5; i++) assertThat(service.consume("lock@example.test", OtpPurpose.REGISTER, "x")).isFalse();
        assertThat(saved.get().getAttempts()).isEqualTo(5);
        assertThat(service.consume("lock@example.test", OtpPurpose.REGISTER, code.getValue())).isFalse();
    }

    static class MutableClock extends Clock {
        private Instant current = Instant.parse("2026-09-28T12:00:00Z");
        void advanceSeconds(long seconds) { current = current.plusSeconds(seconds); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
    }
}
