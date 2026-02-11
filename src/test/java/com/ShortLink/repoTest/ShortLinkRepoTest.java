package com.ShortLink.repoTest;

import com.ShortLink.entity.Link;
import com.ShortLink.repositories.ShortLinkRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ShortLinkRepoTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ShortLinkRepo shortLinkRepo;

    @BeforeEach
    void setUp() {
        shortLinkRepo.deleteAll();
    }

    @Test
    void saveLink_ShouldPersistLink() {
        Link link = new Link();
        link.setOriginalUrl("avito.com");
        link.setShortUrl("abc1234");

        Link saved = shortLinkRepo.save(link);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOriginalUrl()).isEqualTo("avito.com");
        assertThat(saved.getShortUrl()).isEqualTo("abc1234");
    }

    @Test
    void findLinkByOriginalUrl_ShouldReturnLink() {
        Link link = new Link();
        link.setOriginalUrl("avito.com");
        link.setShortUrl("xyz7890");
        shortLinkRepo.save(link);

        Optional<Link> found = shortLinkRepo.findLinkByOriginalUrl("avito.com");

        assertThat(found).isPresent();
        assertThat(found.get().getShortUrl()).isEqualTo("xyz7890");
    }

    @Test
    void findLinkByShortUrl_ShouldReturnLink() {
        Link link = new Link();
        link.setOriginalUrl("avito.com");
        link.setShortUrl("qwe1234");
        shortLinkRepo.save(link);

        Optional<Link> found = shortLinkRepo.findLinkByShortUrl("qwe1234");

        assertThat(found).isPresent();
        assertThat(found.get().getOriginalUrl()).isEqualTo("avito.com");
    }

    @Test
    void saveLink_WithDuplicateShortUrl_ShouldThrowDataIntegrityViolationException() {
        Link link1 = new Link();
        link1.setOriginalUrl("avito.com");
        link1.setShortUrl("dup1234");
        shortLinkRepo.save(link1);

        Link link2 = new Link();
        link2.setOriginalUrl("avito.com");
        link2.setShortUrl("dup1234");

        assertThrows(DataIntegrityViolationException.class, () -> shortLinkRepo.saveAndFlush(link2));
    }
}