package com.shortlink.mappers;


import com.ShortLink.models.Link;
import com.shortlink.dto.LinkDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class LinkMapper {

    public com.shortlink.models.Link toLink(LinkDto linkDto){
        Link link = new Link();
        link.setShortUrl(linkDto.getShortUrl());
        link.setOriginalUrl(linkDto.getOriginalUrl());
        return link;
    };

    public LinkDto toDto(Link link){
        LinkDto linkDto = new LinkDto();
        linkDto.setOriginalUrl(link.getOriginalUrl());
        linkDto.setShortUrl(link.getShortUrl());
        return linkDto;
    };

    public List<LinkDto> toDtos(List<Link> links){
        List<LinkDto> linkDtos = new ArrayList<>();
        for (Link link : links) {
            LinkDto linkDto = new LinkDto();
            linkDto.setOriginalUrl(link.getOriginalUrl());
            linkDto.setShortUrl(link.getShortUrl());
        }
        return linkDtos;
    };

}
