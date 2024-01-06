package com.msmsadegh.keycloak.service;

import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.ws.rs.ClientErrorException;
import java.util.List;


@Component
public class KeycloakService {
    public static final String USER_CONFIRMED;


    static {
        USER_CONFIRMED = "user_confirm";
    }

    private final Keycloak keycloakAdmin;
    private final KeycloakApiHandler keycloakApiHandler;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${mop.keycloak.admin.client-id}")
    private String clientId;

    public KeycloakService(Keycloak keycloakAdmin, KeycloakApiHandler keycloakApiHandler) {
        this.keycloakAdmin = keycloakAdmin;
        this.keycloakApiHandler = keycloakApiHandler;
    }

    public KeycloakWebToken getExchangeToken(String userId) {
        return keycloakApiHandler.getExchangeTokenOrThrow(keycloakAdmin.tokenManager().getAccessTokenString(), userId);
    }

    private CredentialRepresentation preparePasswordRepresentation(String password, Boolean temporary) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(temporary);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        return credentialRepresentation;
    }

    public void setPassword(String username, String password) {
        var credentialRepresentation = preparePasswordRepresentation(password, false);
        keycloakAdmin.realm(realm)
                .users()
                .get(keycloakAdmin.realm(realm).users().search(username).get(0).getId())
                .resetPassword(credentialRepresentation);
    }

    private UserRepresentation prepareUserRepresentation(KeycloakUser keycloakUser) {
        UserRepresentation userRepresentation = new UserRepresentation();

        var credentialRepresentation = preparePasswordRepresentation(keycloakUser.getPassword(),
                false);

        userRepresentation.setUsername(keycloakUser.getUsername());
        userRepresentation.setEmail(keycloakUser.getEmail());
        userRepresentation.setFirstName(keycloakUser.getFirstName());
        userRepresentation.setLastName(keycloakUser.getLastName());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setEnabled(true);
        userRepresentation.singleAttribute(USER_CONFIRMED, "false");
        return userRepresentation;
    }

    public void createUser(KeycloakUser keycloakUser) {
        UserRepresentation userRepresentation = prepareUserRepresentation(keycloakUser);

        var response = keycloakAdmin.realm(realm).users().create(userRepresentation);
        if (response.getStatus() != HttpStatus.CREATED.value())
            throw new ClientErrorException(((ClientResponse) response).getReasonPhrase(), response.getStatus());
        response.close();

        /* assign role user to the new user in keycloak, if not remove the created user */
        var userName = keycloakUser.getUsername();
        try {
            assignUserRole(userName, keycloakUser.getRole());
        } catch (Exception any) {
            removeUser(userName);
            throw any;
        }
    }

    public void assignUserRole(String username, String role) {
        var userRepresentation = getUserRepresentationOrThrow(username);

        RoleRepresentation roleRepresentation = keycloakAdmin.realm(realm)
                .roles()
                .get("user")
                .toRepresentation();

        keycloakAdmin.realm(realm).users()
                .get(userRepresentation.getId()).roles().realmLevel().add(List.of(roleRepresentation));
    }

    public UserRepresentation getUserRepresentationOrThrow(String username) {
        return keycloakAdmin.realm(realm).users().search(username)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("username not found"));
    }

    public void removeUser(String username) {
        var userRepresentation = getUserRepresentationOrThrow(username);

        keycloakAdmin.realm(realm).users().get(userRepresentation.getId()).remove();
    }

    public void confirmUser(String username) {
        var userRepresentation = getUserRepresentationOrThrow(username);

        keycloakAdmin.realm(realm).users().get(userRepresentation.getId())
                .update(userRepresentation.singleAttribute(USER_CONFIRMED, "true"));
    }
}
