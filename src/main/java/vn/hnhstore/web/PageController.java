package vn.hnhstore.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    String home() { return "home"; }

    @GetMapping("/login")
    String login() { return "login"; }

    @GetMapping("/admin")
    String admin() { return "admin"; }
}
