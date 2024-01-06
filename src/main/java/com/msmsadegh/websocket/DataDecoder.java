package com.msmsadegh.websocket;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.Reader;

public class DataDecoder implements Decoder.TextStream<WebSocketResponseMessage>{
    @Override
    public WebSocketResponseMessage decode(Reader reader) throws DecodeException, IOException {
        return WebSocket.mapper.readValue(reader, new TypeReference<WebSocketResponseMessage>() {});
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}