package vn.hnhstore.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import vn.hnhstore.product.ProductRepository;
import vn.hnhstore.security.StorePrincipal;
import vn.hnhstore.user.UserRepository;

@Controller
public class PageController {
    private final UserRepository users;
    private final ProductRepository products;
    public PageController(UserRepository users, ProductRepository products) {
        this.users = users; this.products = products;
    }
    @GetMapping("/")
    String home(@AuthenticationPrincipal StorePrincipal actor, Model model) {
        if (actor != null) model.addAttribute("myProducts", products.countByUserId(actor.id()));
        return "home";
    }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/admin")
    String admin(Model model) {
        model.addAttribute("totalUsers", users.count());
        model.addAttribute("totalProducts", products.count());
        return "admin";
    }
}
