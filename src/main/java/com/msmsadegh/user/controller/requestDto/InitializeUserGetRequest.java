package com.msmsadegh.user.controller.requestDto;

import com.msmsadegh.websocket.WebSocketActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InitializeUserGetRequest {
    @NotNull
    @NotBlank
    private String sid;

    @NotNull
    private WebSocketActionType actionType;

    @NotNull
    @NotBlank
    private String businessKey;

    @NotNull
    private Instant timeout;
}