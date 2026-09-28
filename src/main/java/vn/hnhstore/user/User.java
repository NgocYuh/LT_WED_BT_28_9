package vn.hnhstore.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(nullable = false, length = 200)
    private String fullName;
    @Column(length = 500)
    private String images;
    @Column(nullable = false)
    private boolean enabled = true;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    protected User() { }
    public User(String username, String email, String password, String fullName, String images, Role role) {
        this.username = username; this.email = email; this.password = password;
        this.fullName = fullName; this.images = images; this.role = role;
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getImages() { return images; }
    public boolean isEnabled() { return enabled; }
    public Role getRole() { return role; }
}
