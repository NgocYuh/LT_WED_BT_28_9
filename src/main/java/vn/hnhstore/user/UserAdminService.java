package vn.hnhstore.user;

import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hnhstore.product.ProductRepository;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final ProductRepository products;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;

    public UserAdminService(UserRepository users, RoleRepository roles, ProductRepository products,
                            UserMapper mapper, PasswordEncoder encoder) {
        this.users = users; this.roles = roles; this.products = products;
        this.mapper = mapper; this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public Page<UserAdminView> search(String q, int page) {
        return users.search(q == null ? "" : q.trim(), PageRequest.of(Math.max(0, page), 10,
                Sort.by("id").ascending())).map(this::view);
    }

    @Transactional(readOnly = true)
    public long count() { return users.count(); }

    @Transactional(readOnly = true)
    public UserAdminView get(long id) { return view(users.findById(id).orElseThrow()); }

    @Transactional
    public void create(UserAdminForm form) {
        if (form.getInitialPassword() == null || form.getInitialPassword().length() < 8)
            throw new IllegalArgumentException("Mật khẩu ban đầu phải có ít nhất 8 ký tự");
        String username = normalized(form.getUsername());
        String email = normalized(form.getEmail());
        if (username.contains("@") || users.existsByUsernameIgnoreCase(username) || users.existsByEmailIgnoreCase(email))
            throw new IllegalArgumentException("Username hoặc email đã được sử dụng");
        User user = new User(username, email, encoder.encode(form.getInitialPassword()),
                form.getFullName().trim(), null, role(form.getRoleName()));
        if (!form.isEnabled()) user.deactivate();
        users.save(user);
    }

    @Transactional
    public void update(long id, UserAdminForm form, long actorId) {
        User user = users.findById(id).orElseThrow();
        if (id == actorId && (!form.isEnabled() || !"ADMIN".equals(form.getRoleName())))
            throw new IllegalArgumentException("Không thể tự khóa hoặc hạ quyền ADMIN của mình");
        String username = normalized(form.getUsername());
        String email = normalized(form.getEmail());
        if (username.contains("@") || (users.existsByUsernameIgnoreCase(username) && !user.getUsername().equalsIgnoreCase(username))
                || (users.existsByEmailIgnoreCase(email) && !user.getEmail().equalsIgnoreCase(email)))
            throw new IllegalArgumentException("Username hoặc email đã được sử dụng");
        user.updateProfile(username, email, form.getFullName().trim(), role(form.getRoleName()), form.isEnabled());
        if (form.getInitialPassword() != null && !form.getInitialPassword().isBlank()) {
            if (form.getInitialPassword().length() < 8) throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự");
            user.changePassword(encoder.encode(form.getInitialPassword()));
        }
    }

    @Transactional
    public void delete(long id, long actorId) {
        if (id == actorId) throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập");
        if (products.countByUserId(id) > 0) throw new IllegalArgumentException("Hãy xóa sản phẩm của tài khoản trước");
        users.deleteById(id);
    }

    private UserAdminView view(User user) {
        UserAdminView base = mapper.toAdminView(user);
        return new UserAdminView(base.id(), base.username(), base.email(), base.fullName(),
                base.images(), base.enabled(), base.roleName(), products.countByUserId(user.getId()));
    }
    private Role role(String name) {
        if (!"USER".equals(name) && !"ADMIN".equals(name)) throw new IllegalArgumentException("Role không hợp lệ");
        return roles.findByName(name).orElseThrow();
    }
    private String normalized(String text) { return text.trim().toLowerCase(Locale.ROOT); }
}
