package vn.hnhstore.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hnhstore.otp.OtpPurpose;
import vn.hnhstore.otp.OtpService;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;

@Service
public class RecoveryService {
    private final UserRepository users;
    private final OtpService otp;
    private final PasswordEncoder encoder;

    public RecoveryService(UserRepository users, OtpService otp, PasswordEncoder encoder) {
        this.users = users; this.otp = otp; this.encoder = encoder;
    }

    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) return;
        users.findByEmailWithRole(email.trim()).filter(User::isEnabled)
                .ifPresent(user -> otp.send(user.getEmail(), OtpPurpose.RESET));
    }

    @Transactional
    public boolean reset(ResetPasswordForm form) {
        if (form.getEmail() == null || form.getPassword() == null || form.getConfirmPassword() == null
                || !form.getPassword().equals(form.getConfirmPassword())) return false;
        User user = users.findByEmailWithRole(form.getEmail().trim()).filter(User::isEnabled).orElse(null);
        if (user == null || !otp.consume(user.getEmail(), OtpPurpose.RESET, form.getCode())) return false;
        user.changePassword(encoder.encode(form.getPassword()));
        return true;
    }
}
