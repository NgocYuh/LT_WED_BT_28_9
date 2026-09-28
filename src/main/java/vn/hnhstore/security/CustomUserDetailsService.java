package vn.hnhstore.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = users.findByUsernameOrEmailWithRole(login)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return new StorePrincipal(user.getId(), user.getUsername(), user.getEmail(),
                user.getPassword(), user.getFullName(), user.getImages(),
                user.getRole().getName(), user.isEnabled());
    }
}
