package com.ShortLink.LinkGenerator;

import com.ShortLink.repositories.ShortLinkRepo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Getter
@Setter
@Service
public class LinkGeneratorClass {

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private ShortLinkRepo shortLinkRepo;

    public String LinkGenerator() {
        final String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            password.append(chars.charAt(random.nextInt(26)));
        }
        return password.toString();
    }

    public boolean CheckForUniqueness(String shortUrl) {
        return shortLinkRepo.findLinkByShortUrl(shortUrl) == null;
    }
}
