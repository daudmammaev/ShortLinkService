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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(linkGeneratorClass.CheckForUniqueness(generatedShortUrl)).thenReturn(true);
        when(shortLinkRepo.save(any(Link.class))).thenReturn(link);
        when(linkMapper.toDto(any(Link.class))).thenReturn(linkDto);

        LinkDto result = shortLinkServices.createShortLink(originalUrl);

        assertNotNull(result);
        assertEquals(linkDto.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(linkDto.getShortUrl(), result.getShortUrl());

        verify(linkGeneratorClass, times(1)).LinkGenerator();
        verify(linkGeneratorClass, times(1)).CheckForUniqueness(generatedShortUrl);
        verify(shortLinkRepo, times(1)).save(any(Link.class));
        verify(linkMapper, times(1)).toDto(any(Link.class));
    }

    @Test
    void testCreateShortLink_GenerateUniqueAfterRetry() {
        String originalUrl = "https://www.avito.ru";
        String firstGenerated = "dup123";
        String secondGenerated = "uniq456";

        when(linkGeneratorClass.LinkGenerator())
                .thenReturn(firstGenerated)
                .thenReturn(secondGenerated);

        when(linkGeneratorClass.CheckForUniqueness(firstGenerated)).thenReturn(false);
        when(linkGeneratorClass.CheckForUniqueness(secondGenerated)).thenReturn(true);
        when(shortLinkRepo.save(any(Link.class))).thenReturn(link);
        when(linkMapper.toDto(any(Link.class))).thenReturn(linkDto);

        LinkDto result = shortLinkServices.createShortLink(originalUrl);

        assertNotNull(result);
        verify(linkGeneratorClass, times(2)).LinkGenerator();
        verify(linkGeneratorClass, times(2)).CheckForUniqueness(anyString());
    }

    @Test
    void testCreateShortLink_MaxAttemptsReached() {
        String originalUrl = "https://www.avito.ru";

        when(linkGeneratorClass.LinkGenerator()).thenReturn("test123");
        when(linkGeneratorClass.CheckForUniqueness(anyString())).thenReturn(false);

        ShortLinkCreationException exception = assertThrows(
                ShortLinkCreationException.class,
                () -> shortLinkServices.createShortLink(originalUrl)
        );

        assertTrue(exception.getMessage().contains("10 попыток"));
        verify(linkGeneratorClass, times(10)).LinkGenerator();
        verify(linkGeneratorClass, times(10)).CheckForUniqueness(anyString());
        verify(shortLinkRepo, never()).save(any(Link.class));
    }

    @Test
    void testCreateShortLink_DatabaseError() {
        String originalUrl = "https://www.avito.ru";

        when(linkGeneratorClass.LinkGenerator()).thenReturn("abc123");
        when(linkGeneratorClass.CheckForUniqueness("abc123")).thenReturn(true);
        when(shortLinkRepo.save(any(Link.class))).thenThrow(
                new DataIntegrityViolationException("Database error")
        );

        ShortLinkCreationException exception = assertThrows(
                ShortLinkCreationException.class,
                () -> shortLinkServices.createShortLink(originalUrl)
        );

        assertTrue(exception.getMessage().contains("Не удалось сохранить ссылку"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testFindByShortLink_Success() {
        String shortUrl = "abc123";
        when(shortLinkRepo.findLinkByShortUrl(shortUrl)).thenReturn(link);
        when(linkMapper.toDto(link)).thenReturn(linkDto);
        LinkDto result = shortLinkServices.findByShortLink(shortUrl);

        assertNotNull(result);
        assertEquals(linkDto.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(linkDto.getShortUrl(), result.getShortUrl());

        verify(shortLinkRepo, times(1)).findLinkByShortUrl(shortUrl);
        verify(linkMapper, times(1)).toDto(link);
    }

    @Test
    void testFindByShortLink_NotFound() {
        String shortUrl = "nonexistent";
        when(shortLinkRepo.findLinkByShortUrl(shortUrl)).thenReturn(null);

        ShortLinkNotFoundException exception = assertThrows(
                ShortLinkNotFoundException.class,
                () -> shortLinkServices.findByShortLink(shortUrl)
        );

        assertTrue(exception.getMessage().contains("не найдена"));
        verify(shortLinkRepo, times(1)).findLinkByShortUrl(shortUrl);
        verify(linkMapper, never()).toDto(any());
    }

    @Test
    void testFindByShortLink_RepositoryException() {
        String shortUrl = "abc123";
        when(shortLinkRepo.findLinkByShortUrl(shortUrl))
                .thenThrow(new RuntimeException("Database connection failed"));
        ShortLinkNotFoundException exception = assertThrows(
                ShortLinkNotFoundException.class,
                () -> shortLinkServices.findByShortLink(shortUrl)
        );

        assertTrue(exception.getMessage().contains("Ошибка при поиске"));
        assertNotNull(exception.getCause());
    }

    @Test
    void testCreateShortLink_GeneralException() {
        String originalUrl = "https://www.avito.ru";
        when(linkGeneratorClass.LinkGenerator()).thenThrow(new RuntimeException("Unexpected error"));

        ShortLinkCreationException exception = assertThrows(
                ShortLinkCreationException.class,
                () -> shortLinkServices.createShortLink(originalUrl)
        );

        assertTrue(exception.getMessage().contains("Ошибка при создании короткой ссылки"));
        assertNotNull(exception.getCause());
    }
}