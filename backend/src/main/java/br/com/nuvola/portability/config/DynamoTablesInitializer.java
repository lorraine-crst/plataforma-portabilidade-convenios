package br.com.nuvola.portability.config;

import br.com.nuvola.portability.authorization.Authorization;
import br.com.nuvola.portability.operation.IdempotencyRecord;
import br.com.nuvola.portability.operation.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@Component
public class DynamoTablesInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DynamoTablesInitializer.class);

	private final DynamoDbEnhancedClient enhancedClient;
	private final DynamoProperties properties;

	public DynamoTablesInitializer(DynamoDbEnhancedClient enhancedClient, DynamoProperties properties) {
		this.enhancedClient = enhancedClient;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.createTables()) {
			return;
		}
		createIfMissing(DynamoTables.AUTHORIZATIONS, Authorization.class);
		createIfMissing(DynamoTables.OPERATIONS, Operation.class);
		createIfMissing(DynamoTables.IDEMPOTENCY, IdempotencyRecord.class);
	}

	private <T> void createIfMissing(String tableName, Class<T> beanClass) {
		try {
			enhancedClient.table(tableName, TableSchema.fromBean(beanClass)).createTable();
			log.info("Tabela {} criada", tableName);
		} catch (ResourceInUseException e) {
			log.info("Tabela {} já existe", tableName);
		}
	}

}
