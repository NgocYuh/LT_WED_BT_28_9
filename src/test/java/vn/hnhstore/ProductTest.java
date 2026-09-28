package vn.hnhstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.hnhstore.product.ProductImageStore;
import vn.hnhstore.product.Product;
import vn.hnhstore.product.ProductRepository;
import vn.hnhstore.user.RoleRepository;
import vn.hnhstore.user.User;
import vn.hnhstore.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:products;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop",
    "SEED_USER_EMAIL=product-owner@example.test", "SEED_USER_PASSWORD=owner-pass-123",
    "SEED_ADMIN_EMAIL=product-admin@example.test", "SEED_ADMIN_PASSWORD=admin-pass-123"
})
class ProductTest {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;
    @MockitoBean ProductImageStore images;

    @Test void ownerCanUploadSearchReplaceAndDeleteButOtherUserCannot() throws Exception {
        when(images.upload(any())).thenReturn(new ProductImageStore.Image("https://example.test/one.jpg", "hnhstore/products/one"),
                new ProductImageStore.Image("https://example.test/two.jpg", "hnhstore/products/two"));
        User other = users.save(new User("other-product", "other-product@example.test", encoder.encode("other-pass-123"),
                "Other Owner", null, roles.findByName("USER").orElseThrow()));
        MockHttpSession ownerSession = login("product-owner@example.test", "owner-pass-123");
        MockHttpSession otherSession = login("other-product", "other-pass-123");
        MockHttpSession adminSession = login("product-admin@example.test", "admin-pass-123");
        MockMultipartFile image = new MockMultipartFile("image", "one.jpg", "image/jpeg", new byte[]{(byte) 255,(byte) 216,(byte) 255,1});
        mvc.perform(multipart("/products").file(image).session(ownerSession).with(csrf())
                .param("name", "Book One").param("description", "New book").param("price", "12.50"))
                .andExpect(redirectedUrl("/products"));
        long id = products.findAll().getFirst().getId();
        assertThat(products.countByUserId(users.findByEmailWithRole("product-owner@example.test").orElseThrow().getId())).isEqualTo(1);
        String list = mvc.perform(get("/products").session(ownerSession).param("q", "Book"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(list).contains("Book One", "Tổng sản phẩm: 1", "Của tôi: 1");
        mvc.perform(get("/products/" + id + "/edit").session(otherSession)).andExpect(status().isForbidden());
        mvc.perform(post("/products/" + id + "/delete").session(otherSession).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(products.findById(id)).isPresent();
        MockMultipartFile replacement = new MockMultipartFile("image", "two.jpg", "image/jpeg", new byte[]{(byte) 255,(byte) 216,(byte) 255,2});
        mvc.perform(multipart("/products/" + id).file(replacement).session(ownerSession).with(csrf())
                .param("name", "Book Two").param("description", "Updated").param("price", "14.00"))
                .andExpect(redirectedUrl("/products"));
        verify(images).delete("hnhstore/products/one");
        assertThat(products.findById(id).orElseThrow().getImagePublicId()).isEqualTo("hnhstore/products/two");
        mvc.perform(post("/products/" + id + "/delete").session(adminSession).with(csrf()))
                .andExpect(redirectedUrl("/products"));
        verify(images).delete("hnhstore/products/two");
        assertThat(products.findById(id)).isEmpty();
        assertThat(other.getId()).isNotNull();
        User owner = users.findByEmailWithRole("product-owner@example.test").orElseThrow();
        for (int i = 0; i < 11; i++)
            products.save(new Product("Bulk " + i, "Paging check", java.math.BigDecimal.ONE, owner));
        org.springframework.data.domain.Page<?> firstPage = (org.springframework.data.domain.Page<?>)
                mvc.perform(get("/products").session(ownerSession).param("q", "Bulk")
                        .param("page", "0")).andReturn().getModelAndView().getModel().get("products");
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(11);
        String secondPage = mvc.perform(get("/products").session(ownerSession).param("q", "Bulk")
                .param("page", "1")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(secondPage).contains("Trang 2 / 2");
    }

    private MockHttpSession login(String name, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/login").with(csrf()).param("username", name)
                .param("password", password)).andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession(false);
    }
}
