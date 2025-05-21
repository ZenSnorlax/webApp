package org.zensnorlax.common;

public class ZenException extends RuntimeException {
    public ZenException(String message) {
        super(message);
    }

    public ZenException(String message, Throwable cause) {
        super(message, cause);
    }
}
