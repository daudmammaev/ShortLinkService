package IntergrationsTests;


import ShortLink.ShortLinkApplicationTests;
import com.ShortLink.dto.LinkDto;
import com.ShortLink.mappers.LinkMapper;
import com.ShortLink.models.Link;
import com.ShortLink.services.ShortLinkServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {ShortLinkApplicationTests.class})
@AutoConfigureMockMvc
class LinkControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortLinkServices shortLinkServices;

    @MockBean
    private LinkMapper linkMapper;

    @Test
    void createShortLink_ShouldReturnSuccessMessage() throws Exception {

        String requestBody = "{\"originalUrl\": \"https://www.kinopoisk.ru\"}";
        Link link = new Link();

        when(linkMapper.toLink(any(LinkDto.class))).thenReturn(link);



        mockMvc.perform(post("/api/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(linkMapper).toLink(any(LinkDto.class));
        verify(shortLinkServices).createShortLink(link);
    }

    @Test
    void redirectToOriginalUrl_ShouldRedirectToOriginalUrl() throws Exception {

        String shortLink = "abc123";
        String originalUrl = "https://www.kinopoisk.ru";

        Link link = new Link();
        link.setOriginalUrl(originalUrl);

        when(shortLinkServices.findByShortLink(shortLink)).thenReturn(link);


        mockMvc.perform(get("/api/{shortlink}", shortLink))
                .andExpect(redirectedUrl(originalUrl));

        verify(shortLinkServices).findByShortLink(shortLink);
    }
}