package com.shortlink.services;



import com.shortlink.LinkGenerator.LinkGenerator;
import com.shortlink.models.Link;
import com.shortlink.repositories.ShortLinkRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShortLinkServicesImpl implements ShortLinkServices {
    private static final Logger logger = LoggerFactory.getLogger(ShortLinkServicesImpl.class);
    @Autowired
    public ShortLinkRepo shortLinkRepo;

    @Override
    public Link createShortLink(Link link) {
        Link link1 = new  Link();
        link1.setOriginalUrl(link.getOriginalUrl());
        link1.setShortUrl(String.valueOf(new LinkGenerator(link.getOriginalUrl()).getPassword()));

        logger.info(link1.getOriginalUrl());
        logger.info(link1.getShortUrl());

        shortLinkRepo.save(link1);

        return link1;
    }

    @Override
    public Link findByShortLink(String shortLink) {
        return shortLinkRepo.findLinkByShortUrl(shortLink);
    }

}
