package br.com.nuvola.portability.central;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSigner {

	private static final String ALGORITHM = "HmacSHA256";

	private final SecretKeySpec key;

	public HmacSigner(String secret) {
		this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
	}

	public String sign(byte[] message) {
		return HexFormat.of().formatHex(newMac().doFinal(message));
	}

	public String signRequest(String timestamp, byte[] body) {
		Mac mac = newMac();
		mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(mac.doFinal(body));
	}

	private Mac newMac() {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(key);
			return mac;
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Não foi possível preparar o HMAC", e);
		}
	}

}
