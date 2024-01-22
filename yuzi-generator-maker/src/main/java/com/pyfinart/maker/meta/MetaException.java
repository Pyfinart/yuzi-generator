package com.pyfinart.maker.meta;

/**
 * meta元信息的异常
 */
public class MetaException extends RuntimeException{
    public MetaException(String message) {
        super(message);
    }

    public MetaException(String message, Throwable cause) {
        super(message, cause);
    }
}
