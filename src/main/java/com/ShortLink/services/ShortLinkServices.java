package com.ShortLink.services;

import com.ShortLink.dto.LinkDto;
import com.ShortLink.entity.Link;
import com.ShortLink.exceptions.ShortLinkCreationException;
import com.ShortLink.exceptions.ShortLinkNotFoundException;

public interface ShortLinkServices {
    LinkDto createShortLink(String originalUrl) throws ShortLinkCreationException;
    LinkDto findByShortLink(String shortLink) throws ShortLinkNotFoundException;

}
