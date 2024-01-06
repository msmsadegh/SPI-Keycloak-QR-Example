package com.msmsadegh.exception;

public class ExpiredException extends RuntimeException {
    public static final String messageTemplate = "%s expired!";

    public ExpiredException(String objectName) {
        super(String.format(messageTemplate, objectName));
    }
}