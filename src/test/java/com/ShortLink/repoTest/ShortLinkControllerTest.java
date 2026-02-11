package com.ShortLink.repoTest;

import com.ShortLink.dto.LinkDto;
import com.ShortLink.services.ShortLinkServicesImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ShortLinkControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortLinkServicesImpl shortLinkServices;

    @Test
    void createShortLink_ShouldReturnCreatedStatus() throws Exception {
        LinkDto dto = new LinkDto();
        dto.setOriginalUrl("avito.com");
        dto.setShortUrl("abc1234");

        when(shortLinkServices.createShortLink("avito.com")).thenReturn(dto);

        mockMvc.perform(post("/api/create/{originalUrl}", "avito.com"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Короткая ссылка создана: abc1234"));
    }

    @Test
    void redirectToOriginalLink_ShouldRedirect() throws Exception {
        LinkDto dto = new LinkDto();
        dto.setOriginalUrl("avito.com");
        dto.setShortUrl("redirect");

        when(shortLinkServices.getByShortLink("redirect")).thenReturn(dto);

        mockMvc.perform(get("/api/{shortlink}", "redirect"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("avito.com"));
    }
}
