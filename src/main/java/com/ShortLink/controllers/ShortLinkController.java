package com.shortlink.controllers;



import com.shortlink.dto.LinkDto;
import com.shortlink.mappers.LinkMapper;
import com.shortlink.services.ShortLinkServicesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api")
public class ShortLinkController {

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkServicesImpl.class);
    @Autowired
    private LinkMapper linkMapper;
    @Autowired
    public ShortLinkServicesImpl shortLinkServices;

    @PostMapping("/create")
    public ResponseEntity<String> createShortLink(@RequestBody LinkDto linkDto) {
        logger.info(linkDto.getOriginalUrl());
        return ResponseEntity.ok("Короткая ссылка создана : " + shortLinkServices.createShortLink(linkMapper.toLink(linkDto)).getShortUrl());
    }
    @GetMapping("/{shortlink}")
    public RedirectView redirectToOriginalLink(@PathVariable String shortlink) {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(shortLinkServices.findByShortLink(shortlink).getOriginalUrl());
        return redirectView;
    }
}
