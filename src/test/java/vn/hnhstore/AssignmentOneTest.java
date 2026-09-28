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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:assignment1;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "SEED_USER_EMAIL=user@example.test",
    "SEED_USER_PASSWORD=local-test-only-user",
    "SEED_ADMIN_EMAIL=admin@example.test",
    "SEED_ADMIN_PASSWORD=local-test-only-admin"
})
class AssignmentOneTest {
    @Autowired MockMvc mvc;

    @Test void guestAndCsrf() throws Exception {
        String html = mvc.perform(get("/")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("HNHSTORE", "Hoàng Ngọc Huy", "Đăng nhập");
        mvc.perform(post("/login").param("email", "user@example.test")
                .param("password", "local-test-only-user")).andExpect(status().isForbidden());
    }

    @Test void emailLoginRoleAndLogout() throws Exception {
        MvcResult result = mvc.perform(post("/login").with(csrf())
                .param("email", "user@example.test").param("password", "local-test-only-user"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/")).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        String home = mvc.perform(get("/").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(home).contains("user@example.test", "Đăng xuất");
        mvc.perform(get("/admin").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout=true"));
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test void rejectsInvalidLoginAndAllowsAdmin() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email", "user@example.test")
                .param("password", "wrong")).andExpect(redirectedUrl("/login?error=true"));
        mvc.perform(post("/login").with(csrf()).param("email", "missing@example.test")
                .param("password", "wrong")).andExpect(redirectedUrl("/login?error=true"));
        MvcResult result = mvc.perform(post("/login").with(csrf())
                .param("email", "admin@example.test").param("password", "local-test-only-admin"))
                .andExpect(redirectedUrl("/")).andReturn();
        mvc.perform(get("/admin").session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk());
    }
}
