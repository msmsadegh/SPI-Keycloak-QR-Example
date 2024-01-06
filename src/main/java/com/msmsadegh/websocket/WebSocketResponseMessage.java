package com.msmsadegh.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketResponseMessage {
    private WebSocketResponseMessageType type;
    private String message;
}