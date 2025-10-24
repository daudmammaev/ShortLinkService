package com.ShortLink.services;

import com.ShortLink.dto.LinkDto;
import com.ShortLink.models.Link;

public interface ShortLinkServices {
    Link createShortLink(Link link);
    Link findByShortLink(String shortLink);

}
