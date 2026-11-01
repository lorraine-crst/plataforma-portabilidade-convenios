package br.com.nuvola.portability.operation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

final class RequestFingerprint {

    private RequestFingerprint() {
    }

    static String of(PortabilityRequest request) {
        DestinationAccountRequest account = request.contaDestino();
        String canonical = String.join("|",
                request.beneficiarioId(),
                String.join(",", request.normalizedInstallmentIds()),
                account.banco(),
                account.agencia(),
                account.conta());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

}
