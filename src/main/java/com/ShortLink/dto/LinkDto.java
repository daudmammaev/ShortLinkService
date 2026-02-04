package com.shortlink.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LinkDto {
    private String originalUrl;
    private String shortUrl;
}
