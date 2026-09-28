package vn.hnhstore.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    @Query("select u from User u join fetch u.role where lower(u.username) = lower(:login) or lower(u.email) = lower(:login)")
    Optional<User> findByUsernameOrEmailWithRole(@Param("login") String login);

    @Query("select u from User u join fetch u.role where lower(u.email) = lower(:email)")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query(value = "select u from User u join fetch u.role where lower(u.username) like lower(concat('%', :keyword, '%')) or lower(u.email) like lower(concat('%', :keyword, '%')) or lower(u.fullName) like lower(concat('%', :keyword, '%'))",
           countQuery = "select count(u) from User u where lower(u.username) like lower(concat('%', :keyword, '%')) or lower(u.email) like lower(concat('%', :keyword, '%')) or lower(u.fullName) like lower(concat('%', :keyword, '%'))")
    Page<User> search(@Param("keyword") String keyword, Pageable pageable);
}
