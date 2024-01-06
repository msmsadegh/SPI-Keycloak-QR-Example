package com.msmsadegh.websocket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WebSocketActionType {
    LOGIN("LOGIN"),
    ;

    private final String name;
    private WebSocketActionType(final String name) {
        this.name = name;
    }
    @JsonValue
    public String getName() { return name; }
}