package vn.hnhstore;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import vn.hnhstore.otp.OtpMailSender;
import vn.hnhstore.otp.OtpPurpose;
import vn.hnhstore.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:registration;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RegistrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @MockitoBean OtpMailSender mail;

    @Test void accountRequiresEmailVerificationAndCodeIsSingleUse() throws Exception {
        String email = "new-register@example.test";
        mvc.perform(post("/register").with(csrf()).param("username", "newregister")
                .param("email", email).param("fullName", "New User")
                .param("password", "secure-pass-123").param("confirmPassword", "secure-pass-123"))
                .andExpect(redirectedUrl("/verify-otp?email=new-register%40example.test"));
        assertThat(users.findByEmailWithRole(email).orElseThrow().isEnabled()).isFalse();
        mvc.perform(post("/login").with(csrf()).param("username", email)
                .param("password", "secure-pass-123"))
                .andExpect(redirectedUrl("/login?error=true"));
        mvc.perform(post("/verify-otp").with(csrf()).param("email", email).param("code", "000000"));
        assertThat(users.findByEmailWithRole(email).orElseThrow().isEnabled()).isFalse();
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(mail).send(eq(email), code.capture(), eq(OtpPurpose.REGISTER));
        mvc.perform(post("/verify-otp").with(csrf()).param("email", email)
                .param("code", code.getValue())).andExpect(redirectedUrl("/login"));
        assertThat(users.findByEmailWithRole(email).orElseThrow().isEnabled()).isTrue();
        mvc.perform(post("/login").with(csrf()).param("username", email)
                .param("password", "secure-pass-123")).andExpect(redirectedUrl("/"));
    }
}
