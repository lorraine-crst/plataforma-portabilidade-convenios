package br.com.nuvola.portability.central;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HmacSignerTest {

	@Test
	void matchesRfc4231ReferenceVector() {
		HmacSigner signer = new HmacSigner("Jefe");

		String signature = signer.sign("what do ya want for nothing?".getBytes(StandardCharsets.UTF_8));

		assertThat(signature).isEqualTo("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843");
	}

	@Test
	void signsTimestampDotBody() {
		HmacSigner signer = new HmacSigner("test-hmac-secret");
		byte[] body = "{\"beneficiarioId\":\"BEN001\"}".getBytes(StandardCharsets.UTF_8);

		String signature = signer.signRequest("1700000000", body);

		assertThat(signature).isEqualTo("883b2e7a7b1a9f6e1dd74ade8e60921fb369f897f2313ef76e5f4a34a137548c");
	}

	@Test
	void changesWhenBodyChanges() {
		HmacSigner signer = new HmacSigner("test-hmac-secret");

		String original = signer.signRequest("1700000000", "{\"conta\":\"111\"}".getBytes(StandardCharsets.UTF_8));
		String tampered = signer.signRequest("1700000000", "{\"conta\":\"999\"}".getBytes(StandardCharsets.UTF_8));

		assertThat(tampered).isNotEqualTo(original);
	}

}