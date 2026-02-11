package com.ShortLink.exceptions;

public class ShortLinkCreationException extends ShortLinkException {
    public ShortLinkCreationException(String message) {
        super(message);
    }

    public ShortLinkCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}