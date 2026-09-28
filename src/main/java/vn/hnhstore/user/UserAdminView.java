package vn.hnhstore.user;

public record UserAdminView(Long id, String username, String email, String fullName,
                            String images, boolean enabled, String roleName, long productCount) { }
