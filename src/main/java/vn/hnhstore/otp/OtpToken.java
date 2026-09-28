package vn.hnhstore.otp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
public class OtpToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String email;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false, length = 30)
    private OtpPurpose purpose;
    @Column(nullable = false, length = 100)
    private String otpHash;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private int attempts;
    @Column(nullable = false)
    private boolean used;
    @Version @Column(nullable = false)
    private long version;

    protected OtpToken() { }
    public OtpToken(String email, OtpPurpose purpose, String otpHash, LocalDateTime now) {
        this.email = email; this.purpose = purpose; this.otpHash = otpHash;
        this.createdAt = now; this.expiresAt = now.plusMinutes(10);
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public OtpPurpose getPurpose() { return purpose; }
    public String getOtpHash() { return otpHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getAttempts() { return attempts; }
    public boolean isUsed() { return used; }
    public void recordFailure() { attempts++; }
    public void consume() { used = true; }
}
