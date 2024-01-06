package com.msmsadegh.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.Writer;

public class DataEncoder implements Encoder.TextStream<WebSocketResponseMessage>{

    @Override
    public void encode(WebSocketResponseMessage webSocketResponseMessage, Writer writer) throws EncodeException, IOException {
        WebSocket.mapper.writeValue(writer, webSocketResponseMessage);
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}