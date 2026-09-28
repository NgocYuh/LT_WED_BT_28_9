package vn.hnhstore.user;

public record UserView(Long id, String username, String email, String fullName,
                       String images, String roleName) { }
