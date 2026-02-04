package com.UnitTests;

import com.ShortLink.models.Link;
import com.ShortLink.repositories.ShortLinkRepo;
import com.ShortLink.services.ShortLinkServicesImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortLinkServicesImplTest {

    @Mock
    private ShortLinkRepo shortLinkRepo;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShortLinkServicesImpl shortLinkServices;

    @Test
    void createShortLink_ShouldGenerateShortUrlAndSaveToRepository() {
        // Arrange
        String originalUrl = "https://example.com";
        Link inputLink = new Link();
        inputLink.setOriginalUrl(originalUrl);

        Link savedLink = new Link();
        savedLink.setId(1L);
        savedLink.setOriginalUrl(originalUrl);
        savedLink.setShortUrl("generated123");

        when(shortLinkRepo.save(any(Link.class))).thenReturn(savedLink);

        Link result = shortLinkServices.createShortLink(inputLink);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertNotNull(result.getShortUrl());

        verify(shortLinkRepo).save(any(Link.class));

        ArgumentCaptor<Link> linkCaptor = ArgumentCaptor.forClass(Link.class);
        verify(shortLinkRepo).save(linkCaptor.capture());

        Link capturedLink = linkCaptor.getValue();
        assertEquals(originalUrl, capturedLink.getOriginalUrl());
        assertNotNull(capturedLink.getShortUrl());
        assertTrue(capturedLink.getShortUrl().length() > 0);
    }

    @Test
    void createShortLink_ShouldGenerateDifferentShortUrlsForSameOriginalUrl() {

        String originalUrl = "https://example.com";
        Link inputLink = new Link();
        inputLink.setOriginalUrl(originalUrl);

        when(shortLinkRepo.save(any(Link.class))).thenAnswer(invocation -> {
            Link link = invocation.getArgument(0);
            link.setId(1L);
            return link;
        });

        Link result1 = shortLinkServices.createShortLink(inputLink);
        Link result2 = shortLinkServices.createShortLink(inputLink);

        assertNotNull(result1.getShortUrl());
        assertNotNull(result2.getShortUrl());

        verify(shortLinkRepo, times(2)).save(any(Link.class));
    }

    @Test
    void createShortLink_ShouldHandleNullInputGracefully() {

        assertThrows(NullPointerException.class, () -> {
            shortLinkServices.createShortLink(null);
        });
    }

    @Test
    void createShortLink_ShouldHandleEmptyUrl() {

        Link inputLink = new Link();
        inputLink.setOriginalUrl("");

        when(shortLinkRepo.save(any(Link.class))).thenAnswer(invocation -> {
            Link link = invocation.getArgument(0);
            link.setId(1L);
            return link;
        });

        Link result = shortLinkServices.createShortLink(inputLink);

        assertNotNull(result);
        assertEquals("", result.getOriginalUrl());
        assertNotNull(result.getShortUrl());
        verify(shortLinkRepo).save(any(Link.class));
    }

    @Test
    void findByShortLink_ShouldReturnLinkFromRepository() {

        String shortLink = "abc123";
        Link expectedLink = new Link();
        expectedLink.setId(1L);
        expectedLink.setOriginalUrl("https://example.com");
        expectedLink.setShortUrl(shortLink);

        when(shortLinkRepo.findLinkByShortUrl(shortLink)).thenReturn(expectedLink);

        Link result = shortLinkServices.findByShortLink(shortLink);

        assertNotNull(result);
        assertEquals(expectedLink, result);
        assertEquals(shortLink, result.getShortUrl());
        verify(shortLinkRepo).findLinkByShortUrl(shortLink);
    }

    @Test
    void findByShortLink_ShouldReturnNullWhenLinkNotFound() {

        String nonExistentShortLink = "nonexistent";

        when(shortLinkRepo.findLinkByShortUrl(nonExistentShortLink)).thenReturn(null);

        Link result = shortLinkServices.findByShortLink(nonExistentShortLink);

        assertNull(result);
        verify(shortLinkRepo).findLinkByShortUrl(nonExistentShortLink);
    }

    @Test
    void findByShortLink_ShouldHandleEmptyString() {

        String emptyShortLink = "";

        when(shortLinkRepo.findLinkByShortUrl(emptyShortLink)).thenReturn(null);

        Link result = shortLinkServices.findByShortLink(emptyShortLink);

        assertNull(result);
        verify(shortLinkRepo).findLinkByShortUrl(emptyShortLink);
    }

    @Test
    void findByShortLink_ShouldHandleNullInput() {

        when(shortLinkRepo.findLinkByShortUrl(null)).thenReturn(null);

        Link result = shortLinkServices.findByShortLink(null);

        assertNull(result);
        verify(shortLinkRepo).findLinkByShortUrl(null);
    }

    @Test
    void createShortLink_ShouldVerifyLogging() {

        String originalUrl = "https://example.com";
        Link inputLink = new Link();
        inputLink.setOriginalUrl(originalUrl);

        when(shortLinkRepo.save(any(Link.class))).thenAnswer(invocation -> {
            Link link = invocation.getArgument(0);
            link.setId(1L);
            return link;
        });

        Link result = shortLinkServices.createShortLink(inputLink);

        assertNotNull(result);
        verify(shortLinkRepo).save(any(Link.class));
    }

    @Test
    void createShortLink_ShouldGenerateValidShortUrlFormat() {

        String originalUrl = "https://example.com";
        Link inputLink = new Link();
        inputLink.setOriginalUrl(originalUrl);

        when(shortLinkRepo.save(any(Link.class))).thenAnswer(invocation -> {
            Link link = invocation.getArgument(0);
            link.setId(1L);
            return link;
        });

        Link result = shortLinkServices.createShortLink(inputLink);

        assertNotNull(result.getShortUrl());
        assertFalse(result.getShortUrl().isEmpty());
        assertTrue(result.getShortUrl().matches("[a-zA-Z0-9]+"));
    }
}