package com.msmsadegh.user.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.mop.account.ResponseTemplateDto;
import org.mop.account.user.controller.requestDto.*;
import org.mop.account.user.service.UserService;
import org.mop.account.websocket.WebSocket;
import org.mop.account.websocket.WebSocketDto;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@SecurityRequirement(name = "requiredTokenAPI")
@RequestMapping("/api/")
@Validated
public class UserController {
    private final UserService userService;
    private final WebSocket webSocket;

    public UserController(UserService userService, WebSocket webSocket) {
        this.userService = userService;
        this.webSocket = webSocket;
    }

    @PostMapping("v1/users/login")
    public ResponseTemplateDto<Void> login(@Valid @RequestBody LoginPostRequest loginPostRequest) {
        return userService.login(loginPostRequest);
    }

    @PostMapping("v1/users/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseTemplateDto<Void> signUp(@Valid @RequestBody SignUpPostRequest signUpPostRequest) {
        return userService.signUp(signUpPostRequest);
    }

    @PutMapping("v1/users/verification-code")
    public ResponseTemplateDto<Void> confirmVerificationCode(@Valid @RequestBody VerificationPutRequest verificationPutRequest) {
        return userService.confirmVerificationCode(verificationPutRequest);
    }

    @PutMapping("v1/users/verification-code/resend")
    public ResponseTemplateDto<Void> resendVerificationCode(@Valid @RequestBody ResendVerificationPutRequest verificationPutRequest) {
        return userService.resendVerificationCode(verificationPutRequest);
    }

    @PostMapping("v1/users/web-sockets")
    @ResponseStatus(HttpStatus.CREATED)
    public WebSocketDto createWebSocket(KeycloakAuthenticationToken authentication,
                                        @Valid @RequestBody PostWebSocketRequest postWebSocketRequest) {
        SimpleKeycloakAccount account = (SimpleKeycloakAccount) authentication.getDetails();
        AccessToken token = account.getKeycloakSecurityContext().getToken();
//        logContext.addCallerEmail(token.getEmail());
        return webSocket.getAuthorizedSessionId(token.getEmail(), postWebSocketRequest.getActionType(),
                postWebSocketRequest.getBusinessKey(), postWebSocketRequest.getComment());
    }

    @DeleteMapping("v1/users/web-sockets")
    public void removeWebSocket(KeycloakAuthenticationToken authentication,
                                @PathVariable(value = "sid") String sid,
                                @Valid @RequestBody DeleteWebSocketRequest deleteWebSocketRequest) {
        SimpleKeycloakAccount account = (SimpleKeycloakAccount) authentication.getDetails();
        AccessToken token = account.getKeycloakSecurityContext().getToken();
//        logContext.addCallerEmail(token.getEmail());
        webSocket.closeSession(sid, token.getEmail(), deleteWebSocketRequest.getActionType(),
                deleteWebSocketRequest.getBusinessKey(), deleteWebSocketRequest.getTimeout());
    }

    @GetMapping("v1/qr-code-login")
    public void qrCodeLogin(@Valid @RequestBody InitializeUserGetRequest initializeUserGetRequest,
                            KeycloakAuthenticationToken authentication) {
        SimpleKeycloakAccount account = (SimpleKeycloakAccount) authentication.getDetails();
        AccessToken token = account.getKeycloakSecurityContext().getToken();
//        logContext.addCallerEmail(token.getEmail());
        webSocket.isValidSessionOrThrow(initializeUserGetRequest.getSid(), token.getEmail(), initializeUserGetRequest.getActionType(),
                initializeUserGetRequest.getBusinessKey(), initializeUserGetRequest.getTimeout());
        userService.qrCodeLogin(initializeUserGetRequest, token.getPreferredUsername());
    }
}