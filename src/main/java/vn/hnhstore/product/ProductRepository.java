package vn.hnhstore.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p join fetch p.user where lower(p.name) like lower(concat('%', :keyword, '%')) or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%'))")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);
    long countByUserId(Long userId);
}
