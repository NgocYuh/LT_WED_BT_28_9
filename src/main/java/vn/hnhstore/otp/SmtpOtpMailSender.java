package vn.hnhstore.otp;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpOtpMailSender implements OtpMailSender {
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final Environment environment;

    public SmtpOtpMailSender(ObjectProvider<JavaMailSender> mailSenders, Environment environment) {
        this.mailSenders = mailSenders; this.environment = environment;
    }

    @Override
    public void send(String email, String code, OtpPurpose purpose) {
        String host = environment.getProperty("MAIL_HOST", "");
        String from = environment.getProperty("MAIL_FROM", "");
        if (host.isBlank() || from.isBlank()) {
            throw new IllegalStateException("SMTP chưa được cấu hình");
        }
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (sender == null) throw new IllegalStateException("SMTP chưa được cấu hình");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject(purpose == OtpPurpose.REGISTER
                ? "HNHSTORE - Xác nhận đăng ký" : "HNHSTORE - Đặt lại mật khẩu");
        message.setText("Mã xác thực HNHSTORE: " + code + "\nMã có hiệu lực 10 phút.");
        sender.send(message);
    }
}
