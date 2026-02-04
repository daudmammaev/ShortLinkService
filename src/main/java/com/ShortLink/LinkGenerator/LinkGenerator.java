package com.ShortLink.LinkGenerator;

import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.Random;

@Getter
@Setter
public class LinkGenerator {
    StringBuilder password;
    private static final SecureRandom random = new SecureRandom();
    public LinkGenerator(String link) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder password1 = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            password1.append(chars.charAt(random.nextInt(26)));
        }
        password = new StringBuilder(password1.toString());
    }

}
