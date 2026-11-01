package br.com.nuvola.portability.authorization;

import br.com.nuvola.portability.config.DynamoTables;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class AuthorizationRepository {

	private final DynamoDbTable<Authorization> table;

	public AuthorizationRepository(DynamoDbEnhancedClient enhancedClient) {
		this.table = enhancedClient.table(DynamoTables.AUTHORIZATIONS, TableSchema.fromBean(Authorization.class));
	}

	public void save(Authorization authorization) {
		table.putItem(authorization);
	}

	public Optional<Authorization> findByBeneficiaryId(String beneficiaryId) {
		Key key = Key.builder().partitionValue(beneficiaryId).build();
		return Optional.ofNullable(table.getItem(key));
	}

}
