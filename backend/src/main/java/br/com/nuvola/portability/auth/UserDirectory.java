package br.com.nuvola.portability.auth;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserDirectory {

	private static final String DEFAULT_PASSWORD_HASH = "$2a$10$0phkv4bwaZDe01mrliTGSehuVRuok27cl2E8yTYO5/kQPG/8YccB2";

	private final Map<String, UserAccount> users = List.of(
					new UserAccount("operador", DEFAULT_PASSWORD_HASH, Role.OPERADOR, "Operador Nuvola"),
					new UserAccount("BEN001", DEFAULT_PASSWORD_HASH, Role.BENEFICIARIO, "Lorraine Cristina"),
					new UserAccount("BEN002", DEFAULT_PASSWORD_HASH, Role.BENEFICIARIO, "Ada Lovelace"),
					new UserAccount("BEN_ERRO", DEFAULT_PASSWORD_HASH, Role.BENEFICIARIO, "Santos Dumont"))
			.stream()
			.collect(Collectors.toMap(user -> normalize(user.username()), Function.identity()));

	public Optional<UserAccount> findByUsername(String username) {
		if (username == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(users.get(normalize(username)));
	}

	private static String normalize(String username) {
		return username.trim().toUpperCase(Locale.ROOT);
	}

}
