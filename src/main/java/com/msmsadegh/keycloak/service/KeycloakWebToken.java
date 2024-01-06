package com.msmsadegh.keycloak.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)

public class KeycloakWebToken implements Serializable {
    private String  accessToken;
    private String  refreshToken;
    private Integer  expiresIn;
    private Integer refreshExpiresIn;
    private String  tokenType;
    @JsonProperty("not-before-policy")
    private Integer  notBeforePolicy;
    private String  sessionState;
    private String  scope;
}
