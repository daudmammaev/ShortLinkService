package com.ShortLink.services;

import com.ShortLink.LinkGenerator.LinkGeneratorClass;
import com.ShortLink.dto.LinkDto;
import com.ShortLink.entity.Link;
import com.ShortLink.exceptions.ShortLinkCreationException;
import com.ShortLink.exceptions.ShortLinkNotFoundException;
import com.ShortLink.mappers.LinkMapper;
import com.ShortLink.repositories.ShortLinkRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;


@Service
public class ShortLinkServicesImpl implements ShortLinkServices {

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkServicesImpl.class);

    @Autowired
    public ShortLinkRepo shortLinkRepo;

    @Autowired
    private LinkMapper linkMapper;

    @Autowired
    public LinkGeneratorClass linkGeneratorClass;

    @Override
    public LinkDto createShortLink(String originalUrl) throws ShortLinkCreationException {
        try {
            Link newLink = new Link();
            newLink.setOriginalUrl(originalUrl);
            newLink.setShortUrl(linkGeneratorClass.LinkGenerator());

            int maxAttempts = 10;
            int attempts = 0;

            while (attempts < maxAttempts) {
                if (linkGeneratorClass.CheckForUniqueness(newLink.getShortUrl())) {
                    try {
                        Link savedLink = shortLinkRepo.save(newLink);
                        logger.info("Создана ссылка: original={}, short={}",
                                savedLink.getOriginalUrl(), savedLink.getShortUrl());
                        return linkMapper.toDto(savedLink);
                    } catch (DataAccessException e) {
                        throw new ShortLinkCreationException(
                                "Не удалось сохранить ссылку в базу данных", e);
                    }
                } else {
                    newLink.setShortUrl(linkGeneratorClass.LinkGenerator());
                    attempts++;
                }
            }

            throw new ShortLinkCreationException(
                    "Не удалось сгенерировать уникальную короткую ссылку после " + maxAttempts + " попыток");

        } catch (Exception e) {
            if (e instanceof ShortLinkCreationException) {
                throw e;
            }
            throw new ShortLinkCreationException("Ошибка при создании короткой ссылки", e);
        }
    }

    @Override
    public LinkDto findByShortLink(String shortLink) throws ShortLinkNotFoundException {
        try {
            Link link = shortLinkRepo.findLinkByShortUrl(shortLink);

            logger.info("Поиск ссылки : {}", link.getOriginalUrl());

            if (link.getShortUrl() == null || link.getOriginalUrl() == null) {
                throw new ShortLinkNotFoundException(
                        "Короткая ссылка не найдена: " + shortLink);
            }
            return linkMapper.toDto(link);
        } catch (ShortLinkNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ShortLinkNotFoundException(
                    "Ошибка при поиске короткой ссылки: " + shortLink, e);
        }
    }

}
