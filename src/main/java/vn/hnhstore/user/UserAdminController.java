package vn.hnhstore.user;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.hnhstore.security.StorePrincipal;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserAdminService service;
    public UserAdminController(UserAdminService service) { this.service = service; }

    @GetMapping
    String list(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("users", service.search(q, page));
        model.addAttribute("q", q);
        model.addAttribute("totalUsers", service.count());
        return "users/list";
    }

    @GetMapping("/new")
    String newForm(Model model) {
        if (!model.containsAttribute("form")) model.addAttribute("form", new UserAdminForm());
        model.addAttribute("title", "Thêm user");
        model.addAttribute("action", "/admin/users");
        return "users/form";
    }

    @PostMapping
    String create(@Valid @ModelAttribute("form") UserAdminForm form, BindingResult result,
                  Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try { service.create(form); }
            catch (IllegalArgumentException ex) { result.reject("user.invalid", ex.getMessage()); }
        }
        if (result.hasErrors()) {
            model.addAttribute("title", "Thêm user"); model.addAttribute("action", "/admin/users");
            return "users/form";
        }
        redirect.addFlashAttribute("success", "Đã thêm user");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    String editForm(@PathVariable long id, Model model) {
        UserAdminView user = service.get(id);
        UserAdminForm form = new UserAdminForm();
        form.setUsername(user.username()); form.setEmail(user.email()); form.setFullName(user.fullName());
        form.setRoleName(user.roleName()); form.setEnabled(user.enabled());
        model.addAttribute("form", form);
        model.addAttribute("title", "Sửa user #" + id);
        model.addAttribute("action", "/admin/users/" + id);
        return "users/form";
    }

    @PostMapping("/{id}")
    String update(@PathVariable long id, @Valid @ModelAttribute("form") UserAdminForm form,
                  BindingResult result, @AuthenticationPrincipal StorePrincipal actor,
                  Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try { service.update(id, form, actor.id()); }
            catch (IllegalArgumentException ex) { result.reject("user.invalid", ex.getMessage()); }
        }
        if (result.hasErrors()) {
            model.addAttribute("title", "Sửa user #" + id); model.addAttribute("action", "/admin/users/" + id);
            return "users/form";
        }
        redirect.addFlashAttribute("success", "Đã cập nhật user");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    String delete(@PathVariable long id, @AuthenticationPrincipal StorePrincipal actor,
                  RedirectAttributes redirect) {
        try { service.delete(id, actor.id()); redirect.addFlashAttribute("success", "Đã xóa user"); }
        catch (IllegalArgumentException ex) { redirect.addFlashAttribute("error", ex.getMessage()); }
        return "redirect:/admin/users";
    }
}
