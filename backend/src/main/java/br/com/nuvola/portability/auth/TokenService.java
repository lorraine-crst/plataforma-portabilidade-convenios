package br.com.nuvola.portability.auth;

import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

	public static final String ROLE_CLAIM = "perfil";
	public static final String NAME_CLAIM = "nome";

	private static final Duration VALIDITY = Duration.ofHours(1);

	private final JwtEncoder jwtEncoder;

	public TokenService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String issue(UserAccount user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("nuvola")
				.subject(user.username())
				.issuedAt(now)
				.expiresAt(now.plus(VALIDITY))
				.claim(ROLE_CLAIM, user.role().name())
				.claim(NAME_CLAIM, user.displayName())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

}
