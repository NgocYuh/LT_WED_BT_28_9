package vn.hnhstore.user;

import java.util.Optional;
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
}
