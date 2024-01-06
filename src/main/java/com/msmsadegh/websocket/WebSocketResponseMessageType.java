package com.msmsadegh.websocket;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WebSocketResponseMessageType {
    SUCCESS("SUCCESS"),
    PROCESSING("PROCESSING"),
    ERROR("ERROR")
    ;

    private final String name;

    private WebSocketResponseMessageType(final String name) {
        this.name = name;
    }
    @JsonValue
    public String getName() { return name; }
}