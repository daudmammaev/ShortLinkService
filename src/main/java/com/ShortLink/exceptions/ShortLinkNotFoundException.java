package com.ShortLink.exceptions;

public class ShortLinkNotFoundException extends ShortLinkException {
    public ShortLinkNotFoundException(String message) {
        super(message);
    }

    public ShortLinkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}