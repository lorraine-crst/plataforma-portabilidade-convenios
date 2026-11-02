# Backend — Nuvola

API da plataforma de portabilidade de convênios, em **Java 21** e **Spring Boot 4.1**. Ela integra com a Central de Convênios (o simulador), guarda os dados no **DynamoDB** e protege as rotas com **JWT**.

← [Voltar ao README principal](../README.md) · Contrato da API: [`docs/api.md`](../docs/api.md)

## Como rodar só o backend

Requer Java 21, o `.env` preenchido na raiz do repositório (ver o README principal) e, rodando, o DynamoDB Local e o simulador da Central:

```bash
docker run --rm -p 8000:8000 amazon/dynamodb-local:3.3.2 -jar DynamoDBLocal.jar -inMemory -sharedDb
node mock-central/mock-central.js
```

Depois, dentro de `backend/`:

```bash
./mvnw spring-boot:run
```

A API sobe em http://localhost:8080, e o Swagger em http://localhost:8080/swagger-ui.html. Fora do Docker, o backend lê o `.env` da raiz sozinho (`spring.config.import`).

## Configuração

| Variável | Padrão | Observação |
|---|---|---|
| `CENTRAL_URL` | `http://localhost:4000` | No Docker Compose: `http://central:4000` |
| `DYNAMO_ENDPOINT` | `http://localhost:8000` | No Docker Compose: `http://dynamodb:8000` |
| `JWT_SECRET` | — (obrigatório) | Pelo menos 32 caracteres, exigência do HS256 |
| `HMAC_SECRET` | — (obrigatório) | Chave da assinatura das mensagens para a Central |
| `SWAGGER_ENABLED` | `true` | `false` desliga o Swagger e o `/v3/api-docs` |

Os timeouts da Central são fixos: 2 s para conectar e 5 s para ler a resposta. A configuração é validada ao subir (**fail fast**): sem um segredo, ou com o `JWT_SECRET` curto demais, o app não sobe e mostra uma mensagem dizendo o que fazer.

## Estrutura

```
br.com.nuvola.portability
├── config/         DynamoDB, RestClient da Central, segurança, JWT, Swagger
├── auth/           login, token, usuários e o usuário logado
├── central/        cliente da Central, DTOs e assinatura HMAC
├── authorization/  opt-in do beneficiário
├── schedule/       agenda de parcelas
├── operation/      portabilidade: criação, acompanhamento, painel, idempotência
└── error/          exceções e handler global
```

## Rotas

| Método | Rota | Quem pode |
|---|---|---|
| POST | `/api/auth/login` | qualquer um |
| GET, POST | `/api/autorizacao` | beneficiário (ele mesmo, pelo token) |
| GET | `/api/beneficiarios/{id}/agenda` | operador |
| POST | `/api/portabilidades` | operador (header `Idempotency-Key` obrigatório) |
| GET | `/api/portabilidades/{id}` | operador, ou o beneficiário dono da operação |
| GET | `/api/portabilidades?status=&beneficiarioId=` | operador e beneficiário (só as dele) |
| GET | `/api/portabilidades/resumo` | operador e beneficiário (só as dele) |

Os corpos e as respostas estão em [`docs/api.md`](../docs/api.md) e no Swagger.

## Tabelas no DynamoDB

Criadas automaticamente ao subir, se ainda não existirem:

| Tabela | Chave | Guarda |
|---|---|---|
| `autorizacoes` | `beneficiarioId` | o opt-in de cada beneficiário (uma por pessoa) |
| `operacoes` | `operacaoId` | a operação, com uma cópia das parcelas, a conta de destino, o status e o histórico de eventos |
| `idempotencia` | `chave` | cada `Idempotency-Key`, a operação que ela gerou e a impressão digital do pedido |

## Decisões técnicas

### Gerais
- **Idioma:** o código é em inglês; o que faz parte de um contrato fica em português (campos JSON, códigos de erro, status, rotas e nomes das tabelas)
- **Contratos separados:** a API, o banco e a Central têm cada um as suas classes (`*Response`, entidades e DTOs `Central*`). Uma mudança na Central não chega ao front
- **Dinheiro em `BigDecimal`**, e datas em ISO (`LocalDate` e `Instant`)

### Erros

Toda resposta de erro tem o formato `{ codigo, mensagem }`. Uma exceção base (`ApiException`) carrega o status e o código, e um único handler (`GlobalExceptionHandler`) trata todas. Os erros 401 e 403 são respondidos pelo próprio Spring Security, no mesmo formato.

| Código | HTTP | Quando |
|---|---|---|
| `VALIDACAO` | 400 | Corpo inválido, JSON mal formado, `Idempotency-Key` ausente ou `?status=` inválido |
| `CREDENCIAIS_INVALIDAS` | 401 | Usuário ou senha incorretos (a mensagem não diz qual dos dois) |
| `SESSAO_EXPIRADA` | 401 | Token ausente, inválido ou vencido |
| `ACESSO_NEGADO` | 403 | Perfil sem permissão, ou beneficiário abrindo a operação de outro |
| `NAO_ENCONTRADO` | 404 | Operação ou rota inexistente, ou beneficiário ainda sem autorização |
| `METODO_NAO_PERMITIDO` | 405 | Método HTTP que a rota não aceita |
| `CONFLITO` | 409 | Mesma `Idempotency-Key` com um pedido diferente |
| `BENEFICIARIO_NAO_AUTORIZADO` | 422 | Operador consultando a agenda ou pedindo a portabilidade de quem não deu opt-in |
| `PARCELA_INVALIDA` | 422 | Parcela que não existe na agenda ou não está `ABERTA` |
| `ERRO_INTERNO` | 500 | Erro inesperado: os detalhes vão para o log, e a mensagem para o usuário é genérica |
| `CENTRAL_INDISPONIVEL` | 503 | A Central não respondeu ou respondeu 5xx, mesmo depois das tentativas |
| `ENVIO_INCERTO` | 504 | A Central não respondeu a tempo ao envio: ela pode ter recebido, então o envio nunca é repetido automaticamente |

`PARCELA_INVALIDA`, `METODO_NAO_PERMITIDO`, `ERRO_INTERNO` e `ENVIO_INCERTO` não estão no contrato original; o front mostra a `mensagem` para esses códigos.

### Login e segurança
- **JWT HS256**, válido por 1 hora, com o login (`sub`), o perfil e o nome. A API não guarda sessão (stateless) e não usa cookie, por isso o CSRF fica desligado: o token vai no header `Authorization`
- **Diretório de usuários próprio:** os 4 usuários fixos, senha com hash **BCrypt** e o nome de exibição junto (o contrato exige o nome no login; o `InMemoryUserDetailsManager` do Spring não tem esse campo). O login não diferencia maiúsculas e minúsculas, mas o token sempre leva o identificador oficial (`BEN001`)
- **Regras por perfil no `SecurityFilterChain`:** uma lista só, que bate com o contrato. O acesso é barrado antes do controller, sempre no formato `{ codigo, mensagem }`
- **"O beneficiário só vê as dele"** fica no service, porque depende de abrir a operação no banco. A permissão é conferida antes de consultar a Central
- **O `beneficiarioId` do beneficiário sempre vem do token**, nunca do corpo ou da URL

### Agenda
- O opt-in é conferido no nosso banco **antes** de chamar a Central. Sem opt-in: 422 e nenhuma chamada externa. A conferência (`AuthorizationService.ensureAuthorized`) é reaproveitada na criação da portabilidade
- As parcelas com data passada não são bloqueadas: o backend só confere se a parcela existe e está `ABERTA`, e a Central decide o resto

### Como a duplicidade é evitada
- `Idempotency-Key` + impressão digital SHA-256 do pedido + gravação condicional (`attribute_not_exists`) no DynamoDB
- Mesma chave com o mesmo pedido → a mesma operação, sem nova chamada à Central. Mesma chave com outro pedido → **409 `CONFLITO`**
- **Ordem:** valida → salva a operação → reserva a chave. Isso garante que:
  - a chave sempre aponta para uma operação que existe
  - uma validação que falha não gasta a chave
  - dois pedidos simultâneos continuam seguros: só um consegue reservar, e o outro desfaz o que criou e devolve o vencedor
  - o reenvio comum nem chama a Central
- `FALHA_ENVIO` pode ser reenviada com a mesma chave (a Central nunca recebeu). `ENVIO_INCERTO` nunca é reenviada (a Central pode ter recebido)
- As parcelas são validadas contra a agenda da Central, e uma cópia delas fica guardada na operação
- **Limite conhecido:** dois reenvios simultâneos de uma operação em `FALHA_ENVIO` poderiam enviar duas vezes; a solução seria uma gravação condicional no status

### Status e histórico
- **7 status internos**, que contam a história real: `ENVIANDO`, `PENDENTE`, `CONFIRMADA`, `REJEITADA`, `FALHA_ENVIO`, `ENVIO_INCERTO` e `NAO_ENCONTRADA_NA_CENTRAL`
- **3 na API**, os do contrato: `CONFIRMADA` e `REJEITADA` aparecem como são; todos os outros aparecem como `PENDENTE`. Os eventos são traduzidos do mesmo jeito, mas guardam a descrição real ("Solicitação registrada", "Aceita pela Central"...)
- O status só muda pelo `operation.changeStatus(...)`, que atualiza o status, o `atualizadaEm` e o histórico juntos. Repetir o mesmo status é ignorado, então o polling não duplica eventos. (O setter do status existe só porque o SDK do DynamoDB exige.)

### Acompanhamento
- A atualização acontece na leitura: ao abrir uma operação `PENDENTE`, o backend consulta a Central e aplica o resultado. Quando nada muda, nada é gravado
- Com a Central fora do ar durante o acompanhamento, a operação é devolvida como está, sem erro; o próximo polling tenta de novo

### Retry
- Retry nativo do Spring Framework 7 (`@Retryable` + `@EnableResilientMethods`), sem biblioteca extra
- 3 tentativas, com backoff exponencial (500 ms → 1000 ms)
- Só a `CentralUnavailableException` é repetida (503, conexão recusada, timeout numa consulta). O timeout no envio da operação vira `ENVIO_INCERTO` e nunca é repetido
- **Efeito conhecido:** com a Central fora do ar, o erro aparece depois de cerca de 1,5 s

### HMAC (integridade)
- **O que é assinado:** `timestamp + "." + bytes exatos do corpo`, com HMAC-SHA256 e o `HMAC_SECRET`, em todo POST para a Central
- **Headers:** `X-Timestamp` (segundos desde 1970) e `X-Assinatura` (hexadecimal)
- **Por que o timestamp:** protege contra replay, porque a Central pode recusar mensagens antigas
- **Por que os bytes exatos:** um interceptor do `RestClient` assina o corpo já serializado, igual ao que vai pela rede
- **Como a Central validaria:** recalculando com o mesmo segredo, comparando em tempo constante (`MessageDigest.isEqual`) e recusando timestamps antigos
- **Limitação:** o simulador não valida a assinatura (ignora headers desconhecidos)

### Painel
- Para o beneficiário, o filtro vem sempre do token, e o `beneficiarioId` da URL é ignorado
- O banco filtra só pelo beneficiário; o status é filtrado depois de atualizar as pendentes e de traduzir para o status público
- O resumo é calculado sobre a mesma lista, então os números sempre batem com o que a lista mostra; as `FALHA_ENVIO` ficam fora dos dois
- **Trade-off:** cada operação `PENDENTE` consulta a Central ao listar. Em produção, a atualização seria feita em segundo plano (job agendado ou webhook)
- **Trade-off:** o `Scan` lê a tabela inteira; em produção, um índice secundário (GSI) por beneficiário

### Logs
Hoje, o backend registra no log apenas os erros inesperados e dois eventos operacionais:
- `log.error` dos erros inesperados (`GlobalExceptionHandler`)
- `log.warn` da Central indisponível no acompanhamento (`OperationTrackingService`)
- `log.info` da criação das tabelas (`DynamoTablesInitializer`)

## Testes

```bash
./mvnw test
```

| Classe | Tipo | O que cobre |
|---|---|---|
| `GlobalExceptionHandlerTest` | MockMvc | Cada exceção vira o status e o `{ codigo, mensagem }` certos; o 500 não vaza detalhes |
| `OperationTest` | Unidade | O `changeStatus` gera um evento por mudança, na ordem, e ignora o status repetido |
| `CentralClientTest` | WireMock | 204/200/404 viram o status certo; 503 e conexão recusada → indisponível; timeout no envio → incerto; a assinatura HMAC chega e confere |
| `CentralClientRetryTest` | WireMock + Spring | 503, 503, 200 funciona com 3 chamadas; desiste depois de 3; timeout no envio = 1 chamada só |
| `HmacSignerTest` | Unidade | Vetor oficial da RFC 4231 e o formato `timestamp.corpo` |
| `AuthSecurityTest` | MockMvc | Login válido e inválido, sem token (401), token inválido (401), perfil errado (403), sem cookie de sessão |
| `AuthorizationServiceTest` / `AuthorizationControllerTest` | Unidade / MockMvc | Autorizar duas vezes chama a Central uma vez só; 201 vs. 200; 404 sem autorização |
| `ScheduleServiceTest` / `ScheduleControllerTest` | Unidade / MockMvc | Sem opt-in → 422 e nenhuma chamada à Central; formato das parcelas; lista vazia |
| `PortabilityServiceTest` / `PortabilityControllerTest` | Unidade / MockMvc | Chave nova, repetida, em conflito, reenvio após `FALHA_ENVIO`, parcela inválida, corrida entre pedidos; 202, 400, 409, 503 |
| `OperationTrackingServiceTest` / `OperationControllerTest` | Unidade / MockMvc | Atualização pela Central, Central fora do ar, "só as dele" (403), lista e resumo |
| `OperationSummaryResponseTest` | Unidade | O resumo conta certo para uma lista conhecida |
| `DynamoRepositoriesTest` | Integração | Os repositórios contra o DynamoDB Local (ver abaixo) |
| `PortabilityApplicationTests` | Spring | A aplicação sobe |

**Teste de integração do DynamoDB:** fica desligado no `./mvnw test` normal (aparece como *skipped*). Para rodar, com o DynamoDB Local ligado:

```bash
./mvnw test -Dtest=DynamoRepositoriesTest -Ddynamo.local=true
```

Ele grava dados de exemplo, que dá para conferir no NoSQL Workbench.

### Teste manual

- **Swagger:** http://localhost:8080/swagger-ui.html. Faça login em `POST /api/auth/login` e cole o token no botão **Authorize**
- **`requests.http`** (extensão REST Client do VS Code): o fluxo completo em sequência, com login, autorização, agenda, portabilidade, reenvio idempotente, conflito, acompanhamento, lista, resumo e os erros 401/403

## Bibliotecas

| Biblioteca | Versão | Para quê |
|---|---|---|
| Java (Temurin) | 21 | Versão LTS (suporte longo) |
| Spring Boot | 4.1.2 | Base da API; traz o Spring Framework 7, o Spring Security 7 e o Jackson 3 |
| Spring Web MVC + Validation | (do Boot) | Controllers REST e validação dos corpos |
| Spring Security + OAuth2 Resource Server | (do Boot) | JWT, regras por perfil, API sem sessão |
| Spring Boot Actuator | (do Boot) | `/actuator/health`, usado no healthcheck do Docker |
| AWS SDK v2 (`dynamodb-enhanced`) | 2.57.8 (via BOM) | Acesso ao DynamoDB, convertendo classes Java em itens |
| springdoc-openapi | 3.2.0 | Swagger gerado a partir do código; a 3.2 é a linha compatível com o Boot 4.1 |
| spring-boot-configuration-processor | (do Boot) | Autocomplete das propriedades próprias (`app.*`) no editor |
| JUnit 5, Mockito, AssertJ, MockMvc | (do Boot) | Testes de unidade e de controller |
| WireMock (standalone) | 3.13.2 | Simula a Central nos testes: 503, timeout, 204, contagem de chamadas |