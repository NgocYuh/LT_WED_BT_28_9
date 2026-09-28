package vn.hnhstore.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;

@Service
public class EmailUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public EmailUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = users.findByEmailWithRole(email)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getName())
                .disabled(!user.isEnabled())
                .build();
    }
}
