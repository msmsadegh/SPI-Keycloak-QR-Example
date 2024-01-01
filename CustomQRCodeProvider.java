package org.mop.account.keycloak.service.qr;

import org.keycloak.provider.Provider;

public interface CustomQRCodeProvider extends Provider {
    String generateQRCode(String userId);
    boolean verifyQRCode(String userId, String verificationCode);
}
