package vn.hnhstore.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductView(Long id, String name, String description, BigDecimal price,
                          String imageUrl, Long userId, String username, LocalDateTime createdAt) { }
