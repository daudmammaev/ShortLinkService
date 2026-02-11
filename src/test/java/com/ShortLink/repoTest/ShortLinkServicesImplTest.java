package com.ShortLink.repoTest;

import com.ShortLink.LinkGenerator.LinkGeneratorClass;
import com.ShortLink.dto.LinkDto;
import com.ShortLink.exceptions.ShortLinkCreationException;
import com.ShortLink.exceptions.ShortLinkNotFoundException;
import com.ShortLink.services.ShortLinkServicesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ShortLinkServicesImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ShortLinkServicesImpl shortLinkServices;

    @MockBean
    private LinkGeneratorClass linkGenerator;

    @Test
    void createShortLink_ShouldReturnLinkDto_WhenUniqueShortGenerated() {
        when(linkGenerator.LinkGenerator()).thenReturn("unique1");

        LinkDto result = shortLinkServices.createShortLink("avito.com");

        assertThat(result).isNotNull();
        assertThat(result.getShortUrl()).isEqualTo("unique1");
        assertThat(result.getOriginalUrl()).isEqualTo("avito.com");
    }
    @Test
    void createShortLink_ShouldRetryAndSucceed_AfterDuplicateException() {
        when(linkGenerator.LinkGenerator())
                .thenReturn("dup123")
                .thenReturn("dup123")
                .thenReturn("final1");

        shortLinkServices.createShortLink("avito.com");

        LinkDto result = shortLinkServices.createShortLink("second.com");

        assertThat(result.getShortUrl()).isEqualTo("final1");
        verify(linkGenerator, times(3)).LinkGenerator();
    }

    @Test
    void createShortLink_ShouldThrowShortLinkCreationException_AfterMaxAttempts() {
        when(linkGenerator.LinkGenerator()).thenReturn("collide");
        shortLinkServices.createShortLink("https://collide.com");

        Mockito.clearInvocations(linkGenerator);

        when(linkGenerator.LinkGenerator()).thenReturn("collide");

        assertThrows(ShortLinkCreationException.class, () ->
                shortLinkServices.createShortLink("https://another.com")
        );

        verify(linkGenerator, times(10)).LinkGenerator();
    }

    @Test
    void findByShortLink_ShouldReturnLinkDto_WhenExists() {
        when(linkGenerator.LinkGenerator()).thenReturn("exist12");
        shortLinkServices.createShortLink("find.me");

        LinkDto found = shortLinkServices.getByShortLink("exist12");

        assertThat(found).isNotNull();
        assertThat(found.getOriginalUrl()).isEqualTo("find.me");
        assertThat(found.getShortUrl()).isEqualTo("exist12");
    }

    @Test
    void findByShortLink_ShouldThrowShortLinkNotFoundException_WhenNotExist() {
        assertThrows(ShortLinkNotFoundException.class, () ->
                shortLinkServices.getByShortLink("nope123")
        );
    }
}