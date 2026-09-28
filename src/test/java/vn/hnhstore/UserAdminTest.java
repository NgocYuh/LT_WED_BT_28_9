package vn.hnhstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;
import vn.hnhstore.user.RoleRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:useradmin;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "SEED_ADMIN_EMAIL=admin-user-test@example.test", "SEED_ADMIN_PASSWORD=admin-test-pass"
})
class UserAdminTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired RoleRepository roles;

    @Test void adminCanManageUsersAndSearchWithRoleProtection() throws Exception {
        mvc.perform(get("/admin/users")).andExpect(status().is3xxRedirection());
        MockHttpSession admin = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                .param("username", "admin-user-test@example.test").param("password", "admin-test-pass"))
                .andExpect(redirectedUrl("/")).andReturn().getRequest().getSession(false);
        mvc.perform(get("/admin/users").session(admin)).andExpect(status().isOk());
        mvc.perform(post("/admin/users").session(admin).with(csrf())
                .param("username", "manageduser").param("email", "managed@example.test")
                .param("fullName", "Managed Person").param("roleName", "USER")
                .param("enabled", "true").param("initialPassword", "managed-pass-123"))
                .andExpect(redirectedUrl("/admin/users"));
        User created = users.findByEmailWithRole("managed@example.test").orElseThrow();
        assertThat(created.getRole().getName()).isEqualTo("USER");
        String html = mvc.perform(get("/admin/users").session(admin).param("q", "Managed Person"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("managed@example.test", "Tổng user:");
        mvc.perform(post("/admin/users/" + created.getId()).session(admin).with(csrf())
                .param("username", "manageduser").param("email", "managed@example.test")
                .param("fullName", "Updated Person").param("roleName", "USER")
                .param("enabled", "true").param("initialPassword", ""))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(encoder.matches("managed-pass-123", users.findById(created.getId()).orElseThrow().getPassword())).isTrue();
        MockHttpSession regular = (MockHttpSession) mvc.perform(post("/login").with(csrf())
                .param("username", "manageduser").param("password", "managed-pass-123"))
                .andExpect(redirectedUrl("/")).andReturn().getRequest().getSession(false);
        mvc.perform(get("/admin/users").session(regular)).andExpect(status().isForbidden());
        mvc.perform(post("/admin/users/" + created.getId() + "/delete").session(regular).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/admin/users/" + created.getId() + "/delete").session(admin).with(csrf()))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(users.findById(created.getId())).isEmpty();
        for (int i = 0; i < 11; i++)
            users.save(new User("bulkuser" + i, "bulk" + i + "@example.test", encoder.encode("bulk-pass-123"),
                    "Bulk User " + i, null, roles.findByName("USER").orElseThrow()));
        org.springframework.data.domain.Page<?> page = (org.springframework.data.domain.Page<?>)
                mvc.perform(get("/admin/users").session(admin).param("q", "Bulk User")
                        .param("page", "1")).andExpect(status().isOk())
                        .andReturn().getModelAndView().getModel().get("users");
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(11);
    }
}
