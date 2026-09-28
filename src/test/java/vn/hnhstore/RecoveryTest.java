package vn.hnhstore;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.hnhstore.otp.OtpMailSender;
import vn.hnhstore.otp.OtpPurpose;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:recovery;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "SEED_USER_EMAIL=recovery@example.test", "SEED_USER_PASSWORD=before-reset-123"
})
class RecoveryTest {
    @Autowired MockMvc mvc;
    @MockitoBean OtpMailSender mail;

    @Test void resetChangesPasswordOnlyForValidSingleUseCode() throws Exception {
        mvc.perform(post("/forgot-password").with(csrf()).param("email", "missing@example.test"))
                .andExpect(redirectedUrl("/forgot-password"));
        mvc.perform(post("/forgot-password").with(csrf()).param("email", "recovery@example.test"))
                .andExpect(redirectedUrl("/forgot-password"));
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mail).send(eq("recovery@example.test"), code.capture(), eq(OtpPurpose.RESET));
        String otp = code.getValue();
        mvc.perform(post("/reset-password").with(csrf()).param("email", "recovery@example.test")
                .param("code", "999999".equals(otp) ? "000000" : "999999")
                .param("password", "after-reset-123").param("confirmPassword", "after-reset-123"));
        mvc.perform(post("/login").with(csrf()).param("username", "recovery@example.test")
                .param("password", "before-reset-123")).andExpect(redirectedUrl("/"));
        mvc.perform(post("/reset-password").with(csrf()).param("email", "recovery@example.test")
                .param("code", otp).param("password", "after-reset-123")
                .param("confirmPassword", "after-reset-123")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/login").with(csrf()).param("username", "recovery@example.test")
                .param("password", "before-reset-123")).andExpect(redirectedUrl("/login?error=true"));
        mvc.perform(post("/login").with(csrf()).param("username", "recovery@example.test")
                .param("password", "after-reset-123")).andExpect(redirectedUrl("/"));
        mvc.perform(post("/reset-password").with(csrf()).param("email", "recovery@example.test")
                .param("code", otp).param("password", "third-reset-123")
                .param("confirmPassword", "third-reset-123"));
        mvc.perform(post("/login").with(csrf()).param("username", "recovery@example.test")
                .param("password", "after-reset-123")).andExpect(redirectedUrl("/"));
    }
}
