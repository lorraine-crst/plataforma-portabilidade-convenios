package br.com.nuvola.portability.authorization;

import br.com.nuvola.portability.auth.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/autorizacao")
public class AuthorizationController {

	private final AuthorizationService authorizationService;
	private final CurrentUser currentUser;

	public AuthorizationController(AuthorizationService authorizationService, CurrentUser currentUser) {
		this.authorizationService = authorizationService;
		this.currentUser = currentUser;
	}

	@GetMapping
	public AuthorizationResponse findMine() {
		return AuthorizationResponse.from(authorizationService.findMine(currentUser.username()));
	}

	@PostMapping
	public ResponseEntity<AuthorizationResponse> authorize() {
		AuthorizationResult result = authorizationService.authorize(currentUser.username());
		HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
		return ResponseEntity.status(status).body(AuthorizationResponse.from(result.authorization()));
	}

}
