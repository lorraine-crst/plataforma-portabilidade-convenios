package br.com.nuvola.portability.auth;

import br.com.nuvola.portability.error.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final UserDirectory users;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(UserDirectory users, PasswordEncoder passwordEncoder, TokenService tokenService) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	public LoginResponse login(LoginRequest request) {
		UserAccount user = users.findByUsername(request.usuario())
				.filter(account -> passwordEncoder.matches(request.senha(), account.passwordHash()))
				.orElseThrow(InvalidCredentialsException::new);
		return new LoginResponse(tokenService.issue(user), user.role(), user.displayName());
	}

}
