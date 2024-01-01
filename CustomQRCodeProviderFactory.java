package org.mop.account.keycloak.service.qr;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

public class CustomQRCodeProviderFactory implements ProviderFactory<CustomQRCodeProvider> {

    @Override
    public CustomQRCodeProvider create(KeycloakSession session) {
        return new CustomQRCodeProviderImpl();
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
