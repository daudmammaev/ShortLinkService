package com.ShortLink.controllers;


import com.ShortLink.dto.LinkDto;
import com.ShortLink.exceptions.ShortLinkCreationException;
import com.ShortLink.exceptions.ShortLinkNotFoundException;
import com.ShortLink.mappers.LinkMapper;
import com.ShortLink.services.ShortLinkServicesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api")
public class ShortLinkController {

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkController.class);

    @Autowired
    public ShortLinkServicesImpl shortLinkServices;

    @PostMapping("/create/{originalUrl}")
    public ResponseEntity<String> createShortLink(@PathVariable String originalUrl) {
        logger.info("Создание короткой ссылки для: {}", originalUrl);
        try {
            LinkDto createdLink = shortLinkServices.createShortLink(originalUrl);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Короткая ссылка создана: " + createdLink.getShortUrl());
        } catch (ShortLinkCreationException e) {
            throw e;
        }
    }
    @GetMapping("/{shortlink}")
    public RedirectView redirectToOriginalLink(@PathVariable String shortlink) {
        try {
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(shortLinkServices.findByShortLink(shortlink).getOriginalUrl());

            return redirectView;

        } catch (ShortLinkNotFoundException e) {
            throw e;
        }
    }
}
