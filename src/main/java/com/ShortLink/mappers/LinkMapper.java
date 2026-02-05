package com.ShortLink.mappers;

import com.ShortLink.dto.LinkDto;
import com.ShortLink.entity.Link;
import org.springframework.stereotype.Component;


@Component
public class LinkMapper {

    public Link toLink(LinkDto linkDto){
        Link link = new Link();
        link.setShortUrl(linkDto.getShortUrl());
        link.setOriginalUrl(linkDto.getOriginalUrl());
        return link;
    }

    public LinkDto toDto(Link link){
        LinkDto linkDto = new LinkDto();
        linkDto.setOriginalUrl(link.getOriginalUrl());
        linkDto.setShortUrl(link.getShortUrl());
        return linkDto;
    }
}
