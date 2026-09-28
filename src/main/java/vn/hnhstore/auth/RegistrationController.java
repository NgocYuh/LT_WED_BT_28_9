package vn.hnhstore.auth;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {
    private final RegistrationService registration;

    public RegistrationController(RegistrationService registration) { this.registration = registration; }

    @GetMapping("/register")
    String registerPage(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    String register(@Valid @ModelAttribute("form") RegisterForm form, BindingResult result,
                    RedirectAttributes redirect) {
        if (result.hasErrors()) return "auth/register";
        try {
            String email = registration.register(form);
            redirect.addAttribute("email", email);
            redirect.addFlashAttribute("success", "Đã gửi OTP. Kiểm tra email để kích hoạt tài khoản.");
            return "redirect:/verify-otp";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            result.reject("register.error", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    String verifyPage(@RequestParam(defaultValue = "") String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    String verify(@RequestParam String email, @RequestParam String code, Model model,
                  RedirectAttributes redirect) {
        if (!registration.verify(email, code)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "OTP không hợp lệ, đã hết hạn hoặc đã quá số lần thử.");
            return "auth/verify-otp";
        }
        redirect.addFlashAttribute("success", "Đã kích hoạt tài khoản. Hãy đăng nhập.");
        return "redirect:/login";
    }

    @PostMapping("/resend-register-otp")
    String resend(@RequestParam String email, RedirectAttributes redirect) {
        try {
            registration.resend(email);
            redirect.addFlashAttribute("success", "Đã gửi lại OTP.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        redirect.addAttribute("email", email);
        return "redirect:/verify-otp";
    }
}
