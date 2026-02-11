package com.ShortLink.ServicesTest;

import com.ShortLink.LinkGenerator.LinkGeneratorClass;
import com.ShortLink.dto.LinkDto;
import com.ShortLink.entity.Link;
import com.ShortLink.exceptions.ShortLinkCreationException;
import com.ShortLink.exceptions.ShortLinkNotFoundException;
import com.ShortLink.mappers.LinkMapper;
import com.ShortLink.repositories.ShortLinkRepo;
import com.ShortLink.services.ShortLinkServicesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortLinkServicesImplTest {

    @Mock
    private ShortLinkRepo shortLinkRepo;

    @Mock
    private LinkMapper linkMapper;

    @Mock
    private LinkGeneratorClass linkGeneratorClass;

    @InjectMocks
    private ShortLinkServicesImpl shortLinkServices;

    private Link link;
    private LinkDto linkDto;

    @BeforeEach
    void setUp() {
        link = new Link();
        link.setId(1L);
        link.setOriginalUrl("https://www.avito.ru");
        link.setShortUrl("abc123");

        linkDto = new LinkDto();
        linkDto.setOriginalUrl("https://www.avito.ru");
        linkDto.setShortUrl("abc123");
    }

    @Test
    void testCreateShortLink_Success() {
        String originalUrl = "https://www.avito.ru";
        String generatedShortUrl = "abc123";

        when(linkGeneratorClass.LinkGenerator()).thenReturn(generatedShortUrl);
        when(shortLinkRepo.save(any(Link.class))).thenReturn(link);
        when(linkMapper.toDto(any(Link.class))).thenReturn(linkDto);

        LinkDto result = shortLinkServices.createShortLink(originalUrl);

        assertNotNull(result);
        assertEquals(linkDto.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(linkDto.getShortUrl(), result.getShortUrl());

        verify(linkGeneratorClass, times(1)).LinkGenerator();
        verify(shortLinkRepo, times(1)).save(any(Link.class));
        verify(linkMapper, times(1)).toDto(any(Link.class));
    }

    @Test
    void testCreateShortLink_DuplicateShortUrl_RetrySuccess() {
        String originalUrl = "https://www.avito.ru";
        String firstGenerated = "dup123";
        String secondGenerated = "uniq456";

        when(linkGeneratorClass.LinkGenerator())
                .thenReturn(firstGenerated)
                .thenReturn(secondGenerated);
        when(shortLinkRepo.save(argThat(link -> link.getShortUrl().equals(firstGenerated))))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));
        when(shortLinkRepo.save(argThat(link -> link.getShortUrl().equals(secondGenerated))))
                .thenReturn(link);
        when(linkMapper.toDto(any(Link.class))).thenReturn(linkDto);

        LinkDto result = shortLinkServices.createShortLink(originalUrl);

        assertNotNull(result);
        verify(linkGeneratorClass, times(2)).LinkGenerator();
        verify(shortLinkRepo, times(2)).save(any(Link.class));
    }

    @Test
    void testCreateShortLink_MaxAttemptsReached() {
        String originalUrl = "https://www.avito.ru";

        when(linkGeneratorClass.LinkGenerator()).thenReturn("test123");
        when(shortLinkRepo.save(any(Link.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        ShortLinkCreationException exception = assertThrows(
                ShortLinkCreationException.class,
                () -> shortLinkServices.createShortLink(originalUrl)
        );

        assertTrue(exception.getMessage().contains("10 попыток"));
        verify(linkGeneratorClass, atLeast(10)).LinkGenerator();
        verify(shortLinkRepo, atLeast(10)).save(any(Link.class));
    }

    @Test
    void createShortLink_ShouldFailAfterMaxAttempts_WhenAlwaysDuplicate() {
        when(linkGeneratorClass.LinkGenerator()).thenReturn("dup123");
        when(shortLinkRepo.save(any(Link.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThrows(ShortLinkCreationException.class,
                () -> shortLinkServices.createShortLink("https://avito.ru"));

        verify(linkGeneratorClass, times(10)).LinkGenerator();
        verify(shortLinkRepo, times(10)).save(any(Link.class));
    }

    @Test
    void testGetByShortLink_Success() {
        String shortUrl = "abc123";
        when(shortLinkRepo.findLinkByShortUrl(shortUrl)).thenReturn(Optional.of(link));
        when(linkMapper.toDto(link)).thenReturn(linkDto);

        LinkDto result = shortLinkServices.getByShortLink(shortUrl);

        assertNotNull(result);
        assertEquals(linkDto.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(linkDto.getShortUrl(), result.getShortUrl());

        verify(shortLinkRepo, times(1)).findLinkByShortUrl(shortUrl);
        verify(linkMapper, times(1)).toDto(link);
    }

    @Test
    void testGetByShortLink_NotFound() {
        String shortUrl = "nonexistent";
        when(shortLinkRepo.findLinkByShortUrl(shortUrl)).thenReturn(Optional.empty());

        ShortLinkNotFoundException exception = assertThrows(
                ShortLinkNotFoundException.class,
                () -> shortLinkServices.getByShortLink(shortUrl)
        );

        assertTrue(exception.getMessage().contains("не найдена"));
        verify(shortLinkRepo, times(1)).findLinkByShortUrl(shortUrl);
        verify(linkMapper, never()).toDto(any());
    }

    @Test
    void testGetByShortLink_RepositoryException() {
        String shortUrl = "abc123";
        when(shortLinkRepo.findLinkByShortUrl(shortUrl))
                .thenThrow(new RuntimeException("Database connection failed"));

        ShortLinkNotFoundException exception = assertThrows(
                ShortLinkNotFoundException.class,
                () -> shortLinkServices.getByShortLink(shortUrl)
        );

        assertTrue(exception.getMessage().contains("Ошибка при поиске"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testGetByShortLink_NullFieldsInLink() {
        String shortUrl = "abc123";
        Link brokenLink = new Link();
        brokenLink.setId(1L);
        brokenLink.setShortUrl(null);
        brokenLink.setOriginalUrl(null);

        when(shortLinkRepo.findLinkByShortUrl(shortUrl)).thenReturn(Optional.of(brokenLink));

        ShortLinkNotFoundException exception = assertThrows(
                ShortLinkNotFoundException.class,
                () -> shortLinkServices.getByShortLink(shortUrl)
        );

        assertTrue(exception.getMessage().contains("пустыми полями"));
        verify(shortLinkRepo, times(1)).findLinkByShortUrl(shortUrl);
    }
}