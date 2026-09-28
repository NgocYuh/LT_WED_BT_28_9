package vn.hnhstore.auth;

import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hnhstore.otp.OtpPurpose;
import vn.hnhstore.otp.OtpService;
import vn.hnhstore.user.Role;
import vn.hnhstore.user.RoleRepository;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final OtpService otp;

    public RegistrationService(UserRepository users, RoleRepository roles, PasswordEncoder encoder, OtpService otp) {
        this.users = users; this.roles = roles; this.encoder = encoder; this.otp = otp;
    }

    @Transactional
    public String register(RegisterForm form) {
        String username = form.getUsername().trim().toLowerCase(Locale.ROOT);
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (users.existsByUsernameIgnoreCase(username) || users.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Username hoặc email đã được sử dụng");
        }
        if (username.contains("@") || !form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Thông tin đăng ký không hợp lệ");
        }
        Role userRole = roles.findByName("USER").orElseThrow();
        User user = new User(username, email, encoder.encode(form.getPassword()), form.getFullName().trim(),
                null, userRole);
        user.deactivate();
        users.save(user);
        otp.send(email, OtpPurpose.REGISTER);
        return email;
    }

    @Transactional
    public boolean verify(String email, String code) {
        User user = users.findByEmailWithRole(email).orElse(null);
        if (user == null || user.isEnabled()) return false;
        if (!otp.consume(email, OtpPurpose.REGISTER, code)) return false;
        user.activate();
        return true;
    }

    @Transactional
    public void resend(String email) {
        User user = users.findByEmailWithRole(email).orElseThrow(() ->
                new IllegalArgumentException("Không tìm thấy tài khoản chờ xác minh"));
        if (user.isEnabled()) throw new IllegalArgumentException("Tài khoản đã được kích hoạt");
        otp.send(user.getEmail(), OtpPurpose.REGISTER);
    }
}
