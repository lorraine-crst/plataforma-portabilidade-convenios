package br.com.nuvola.portability.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nuvola.portability.central.CentralAuthorizationResponse;
import br.com.nuvola.portability.central.CentralClient;
import br.com.nuvola.portability.error.AuthorizationNotFoundException;
import br.com.nuvola.portability.error.CentralUnavailableException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

	@Mock
	private AuthorizationRepository repository;

	@Mock
	private CentralClient centralClient;

	@InjectMocks
	private AuthorizationService service;

	@Test
	void callsCentralOnlyOnceWhenAuthorizingTwice() {
		when(repository.findByBeneficiaryId("BEN001")).thenReturn(Optional.empty());
		when(centralClient.registerAuthorization("BEN001")).thenReturn(new CentralAuthorizationResponse("AUT-1"));

		AuthorizationResult first = service.authorize("BEN001");

		when(repository.findByBeneficiaryId("BEN001")).thenReturn(Optional.of(first.authorization()));

		AuthorizationResult second = service.authorize("BEN001");

		verify(centralClient, times(1)).registerAuthorization("BEN001");
		verify(repository, times(1)).save(any());
		assertThat(first.created()).isTrue();
		assertThat(second.created()).isFalse();
		assertThat(second.authorization().getAuthorizationId()).isEqualTo("AUT-1");
	}

	@Test
	void savesAuthorizationWithCentralIdAndBeneficiaryFromCaller() {
		when(repository.findByBeneficiaryId("BEN002")).thenReturn(Optional.empty());
		when(centralClient.registerAuthorization("BEN002")).thenReturn(new CentralAuthorizationResponse("AUT-2"));

		AuthorizationResult result = service.authorize("BEN002");

		assertThat(result.authorization().getBeneficiaryId()).isEqualTo("BEN002");
		assertThat(result.authorization().getAuthorizationId()).isEqualTo("AUT-2");
		assertThat(result.authorization().getAuthorizedAt()).isNotNull();
	}

	@Test
	void savesNothingWhenCentralIsUnavailable() {
		when(repository.findByBeneficiaryId("BEN001")).thenReturn(Optional.empty());
		when(centralClient.registerAuthorization("BEN001")).thenThrow(new CentralUnavailableException());

		assertThatThrownBy(() -> service.authorize("BEN001"))
				.isInstanceOf(CentralUnavailableException.class);
		verify(repository, never()).save(any());
	}

	@Test
	void throwsNotFoundWhenBeneficiaryHasNotAuthorized() {
		when(repository.findByBeneficiaryId("BEN001")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.findMine("BEN001"))
				.isInstanceOf(AuthorizationNotFoundException.class);
	}

}