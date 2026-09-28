package vn.hnhstore.otp;

public interface OtpMailSender {
    void send(String email, String code, OtpPurpose purpose);
}
