package com.ShortLink.exceptions;

public class ShortLinkException extends RuntimeException {
    public ShortLinkException(String message) {
        super(message);
    }

    public ShortLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}