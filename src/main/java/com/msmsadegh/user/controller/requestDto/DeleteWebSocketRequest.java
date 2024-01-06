package com.msmsadegh.user.controller.requestDto;

import com.msmsadegh.websocket.WebSocketActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteWebSocketRequest {
    @NotNull
    WebSocketActionType actionType;

    @NotNull
    @NotBlank
    String businessKey;

    @NotNull
    Instant timeout;
}