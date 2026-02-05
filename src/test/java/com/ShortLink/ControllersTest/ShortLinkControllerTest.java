package com.ShortLink.ControllersTest;

import com.ShortLink.controllers.ShortLinkController;
import com.ShortLink.dto.LinkDto;
import com.ShortLink.exceptions.ShortLinkCreationException;
import com.ShortLink.exceptions.ShortLinkNotFoundException;
import com.ShortLink.services.ShortLinkServicesImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortLinkController.class)
class ShortLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortLinkServicesImpl shortLinkServices;

    private LinkDto linkDto;

    @BeforeEach
    void setUp() {
        linkDto = new LinkDto();
        linkDto.setOriginalUrl("https://www.avito.ru");
        linkDto.setShortUrl("abc123");
    }

    @Test
    void testCreateShortLink_Success() throws Exception {
        String originalUrl = "https://www.avito.ru";
        when(shortLinkServices.createShortLink(originalUrl)).thenReturn(linkDto);
        mockMvc.perform(post("/api/create/{originalUrl}", originalUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Короткая ссылка создана: abc123"));

        verify(shortLinkServices, times(1)).createShortLink(originalUrl);
    }

    @Test
    void testCreateShortLink_InvalidUrl() throws Exception {
        String invalidUrl = "not-a-valid-url";
        when(shortLinkServices.createShortLink(invalidUrl))
                .thenThrow(new ShortLinkCreationException("Invalid URL"));
        mockMvc.perform(post("/api/create/{originalUrl}", invalidUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(shortLinkServices, times(1)).createShortLink(invalidUrl);
    }

    @Test
    void testCreateShortLink_DuplicateShortUrl() throws Exception {
        String originalUrl = "https://www.avito.ru";
        when(shortLinkServices.createShortLink(originalUrl))
                .thenThrow(new ShortLinkCreationException("Short URL already exists"));
        mockMvc.perform(post("/api/create/{originalUrl}", originalUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRedirectToOriginalLink_Success() throws Exception {
        String shortLink = "abc123";
        when(shortLinkServices.findByShortLink(shortLink)).thenReturn(linkDto);
        mockMvc.perform(get("/api/{shortlink}", shortLink))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.avito.ru"));

        verify(shortLinkServices, times(1)).findByShortLink(shortLink);
    }

    @Test
    void testRedirectToOriginalLink_NotFound() throws Exception {
        String shortLink = "nonexistent";
        when(shortLinkServices.findByShortLink(shortLink))
                .thenThrow(new ShortLinkNotFoundException("Link not found"));
        mockMvc.perform(get("/api/{shortlink}", shortLink))
                .andExpect(status().isNotFound());

        verify(shortLinkServices, times(1)).findByShortLink(shortLink);
    }

    @Test
    void testRedirectToOriginalLink_EmptyShortLink() throws Exception {
        mockMvc.perform(get("/api/{shortlink}", ""))
                .andExpect(status().isNotFound());

        verify(shortLinkServices, never()).findByShortLink(anyString());
    }

    @Test
    void testCreateShortLink_WithEncodedUrl() throws Exception {
        String originalUrl = "https://www.avito.ru/path%20with%20spaces";
        LinkDto linkDtoWithEncoded = new LinkDto();
        linkDtoWithEncoded.setShortUrl("xyz789");

        when(shortLinkServices.createShortLink(originalUrl)).thenReturn(linkDtoWithEncoded);

        mockMvc.perform(post("/api/create/{originalUrl}", originalUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Короткая ссылка создана: xyz789"));
    }

    @Test
    void testCreateShortLink_WithSpecialCharacters() throws Exception {
        String originalUrl = "https://www.avito.ru/search?q=тест&lang=ru";
        LinkDto linkDtoSpecial = new LinkDto();
        linkDtoSpecial.setShortUrl("test12");

        when(shortLinkServices.createShortLink(originalUrl)).thenReturn(linkDtoSpecial);

        mockMvc.perform(post("/api/create/{originalUrl}", originalUrl)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Короткая ссылка создана: test12"));
    }
}