package org.mop.account.keycloak.service.qr;

public class CustomQRCodeProviderImpl implements CustomQRCodeProvider {

    @Override
    public String generateQRCode(String userId) {
        return null;
    }

    @Override
    public boolean verifyQRCode(String userId, String verificationCode) {
        return false;
    }

    public void close() {

    }
}
