package vn.hnhstore.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.hnhstore.user.UserMapper;
import vn.hnhstore.user.UserRepository;

@ControllerAdvice
public class HeaderModel {
    private final UserRepository users;
    private final UserMapper mapper;

    public HeaderModel(UserRepository users, UserMapper mapper) {
        this.users = users; this.mapper = mapper;
    }

    @ModelAttribute
    void addCurrentUser(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            users.findByEmailWithRole(authentication.getName())
                    .map(mapper::toView)
                    .ifPresent(view -> model.addAttribute("currentUser", view));
        }
    }
}
