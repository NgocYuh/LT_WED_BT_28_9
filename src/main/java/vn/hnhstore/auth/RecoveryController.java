package vn.hnhstore.auth;

import jakarta.validation.Valid;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecoveryController {
    private final RecoveryService recovery;
    public RecoveryController(RecoveryService recovery) { this.recovery = recovery; }

    @GetMapping("/forgot-password")
    String forgotPage() { return "auth/forgot-password"; }

    @PostMapping("/forgot-password")
    String forgot(@RequestParam String email, RedirectAttributes redirect) {
        try {
            recovery.requestReset(email);
        } catch (IllegalArgumentException | IllegalStateException | MailException ignored) {
            // The response does not disclose whether the address exists or is rate limited.
        }
        redirect.addFlashAttribute("success", "Nếu email hợp lệ, mã đặt lại mật khẩu đã được gửi.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    String resetPage(@RequestParam(defaultValue = "") String email, Model model) {
        if (!model.containsAttribute("form")) {
            ResetPasswordForm form = new ResetPasswordForm();
            form.setEmail(email);
            model.addAttribute("form", form);
        }
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    String reset(@Valid @ModelAttribute("form") ResetPasswordForm form, BindingResult result,
                 RedirectAttributes redirect) {
        if (result.hasErrors()) return "auth/reset-password";
        if (!recovery.reset(form)) {
            result.reject("otp.invalid", "OTP không hợp lệ hoặc đã hết hạn.");
            return "auth/reset-password";
        }
        redirect.addFlashAttribute("success", "Đã đổi mật khẩu. Hãy đăng nhập.");
        return "redirect:/login";
    }
}
