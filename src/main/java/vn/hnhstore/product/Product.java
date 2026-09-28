package vn.hnhstore.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.Nationalized;
import vn.hnhstore.user.User;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Nationalized @Column(nullable = false, length = 200)
    private String name;
    @Nationalized @Column(length = 1000)
    private String description;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    @Column(length = 1000)
    private String imageUrl;
    @Column(length = 500)
    private String imagePublicId;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    protected Product() { }
    public Product(String name, String description, BigDecimal price, User user) {
        this.name = name; this.description = description; this.price = price; this.user = user;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getImagePublicId() { return imagePublicId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public void update(String name, String description, BigDecimal price) {
        this.name = name; this.description = description; this.price = price;
    }
    public void setImage(String url, String publicId) { imageUrl = url; imagePublicId = publicId; }
}
