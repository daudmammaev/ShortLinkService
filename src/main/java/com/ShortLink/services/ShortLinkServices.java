package com.shortlink.services;


import com.shortlink.models.Link;

public interface ShortLinkServices {
    Link createShortLink(Link link);
    Link findByShortLink(String shortLink);

}
