package com.ShortLink.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LinkDto {
    private String originalUrl;
    private String shortUrl;
}
