# Nuvola — Plataforma de Portabilidade de Convênios

Plataforma fullstack para uma empresa assumir, como nova credora, parcelas de convênios de desconto em folha: captura a autorização do beneficiário, consulta a agenda na Central de Convênios, registra a portabilidade e acompanha a confirmação ou a rejeição.

- **Demonstração:** https://54-20-185-96.sslip.io
- **API (Swagger):** https://54-20-185-96.sslip.io/swagger-ui/index.html
- **Detalhes técnicos:** [backend](backend/README.md) · [frontend](frontend/README.md) · [contrato da API](docs/api.md)

## 1. Como rodar

### Pré-requisitos

- **Com Docker (recomendado):** [Git](https://git-scm.com/downloads) e [Docker Desktop](https://www.docker.com/products/docker-desktop/) (aberto e rodando)
- **Sem Docker:** Git, [Java 21](https://adoptium.net/temurin/releases/?version=21) e [Node 24](https://nodejs.org/)

### Passo 1 — Baixe o projeto

```bash
git clone https://github.com/lorraine-crst/plataforma-portabilidade-convenios.git
cd plataforma-portabilidade-convenios
```

Todos os comandos a seguir partem dessa pasta.

### Passo 2 — Crie o arquivo de segredos

```bash
cp .env.example .env
openssl rand -base64 32
```

Rode o `openssl` **duas vezes** e cole um valor em `JWT_SECRET` e o outro em `HMAC_SECRET`, no arquivo `.env` (abra num editor de texto). Sem os segredos, o backend não sobe e avisa o que falta. No Windows, use o **Git Bash**, que já traz o `openssl`.

### Passo 3 — Suba o projeto

**Opção A — Com Docker:** um comando sobe o banco, o simulador, o backend e o front.

```bash
docker compose up --build
```

A primeira vez demora alguns minutos. Está pronto quando aparecer `Started PortabilityApplication` e, depois, os logs do `frontend`. Para parar: `Ctrl + C` e `docker compose down`.

**Opção B — Sem Docker:** 4 terminais, nesta ordem. Só abra o próximo quando aparecer a mensagem de "pronto".

| Terminal | Comando | Pronto quando aparecer |
|---|---|---|
| 1 — Banco | Baixe o [DynamoDB Local da AWS](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.DownloadingAndRunning.html), descompacte numa pasta fora do projeto e, nela, rode `java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -inMemory -sharedDb` | `Port: 8000` |
| 2 — Simulador da Central | `node mock-central/mock-central.js` | `Central de Convênios (simulada)` |
| 3 — Backend | `cd backend` e `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`) | `Started PortabilityApplication` |
| 4 — Front | `cd frontend`, `npm install` e `npm run dev` | `compiled successfully` |

Para parar: `Ctrl + C` em cada terminal.

### Passo 4 — Use

Abra **http://localhost:3000**. Todos os usuários usam a senha `lola123`:

| Usuário | Perfil | Cenário |
|---|---|---|
| `operador` | Operador | Consulta agendas e pede portabilidades |
| `BEN001` | Beneficiário | 2 parcelas; confirmada em ~5 s |
| `BEN002` | Beneficiário | 1 parcela; rejeitada em ~5 s, com o motivo |
| `BEN_ERRO` | Beneficiário | Agenda vazia; a Central responde 503 no envio (coberto pelos testes) |

**Roteiro:** entre como `BEN001` e autorize → entre como `operador`, em **Nova portabilidade** consulte `BEN001`, marque as parcelas, preencha a conta (banco `001`, agência `1234`, conta `56789-0`) e solicite → acompanhe até **Portabilidade confirmada**. Repita com o `BEN002` para ver a rejeição.

O banco roda em memória: ao parar o projeto, os dados somem.

### Testes

- **Backend:** `cd backend && ./mvnw test` → `BUILD SUCCESS`
- **E2E (com o projeto rodando):** `cd frontend && npm install && npm run e2e` → 5 testes
- **Manual:** Swagger em http://localhost:8080/swagger-ui.html (login em `POST /api/auth/login`, token no botão **Authorize**)

## 2. Decisões técnicas

Os detalhes e os trade-offs estão nos READMEs do [backend](backend/README.md#decisões-técnicas) e do [frontend](frontend/README.md#decisões-técnicas).

- **Evitar duplicidade, em camadas.** No front, uma trava com `useRef` contra o duplo clique e uma `Idempotency-Key` reaproveitada enquanto o pedido for o mesmo. No backend, a chave + a impressão digital SHA-256 do pedido + uma gravação condicional no DynamoDB: o mesmo pedido devolve a mesma operação, sem chamar a Central de novo; outro pedido com a mesma chave dá 409 `CONFLITO`
- **Indisponível vs. incerto.** Se a Central não recebeu o pedido (503, conexão recusada), o backend tenta 3 vezes e responde 503 `CENTRAL_INDISPONIVEL`, e é seguro reenviar. Se ela não respondeu a tempo ao envio, o pedido pode ter chegado: o backend responde 504 `ENVIO_INCERTO` e nunca repete sozinho
- **Espera pela confirmação.** A tela consulta o status a cada 2 s e pausa depois de 60 s, com um botão "Atualizar agora". O backend consulta a Central ao ler a operação
- **Status e histórico.** 7 status internos contam a história real (`FALHA_ENVIO`, `ENVIO_INCERTO`...); a API mostra só os 3 do contrato. Cada mudança vira um evento, exibido no acompanhamento (diferencial)
- **Integridade (diferencial).** Todo POST para a Central é assinado com HMAC-SHA256 sobre o timestamp e os bytes exatos do corpo
- **Segurança.** JWT de 1 hora, senhas com BCrypt, regras por perfil no backend; o beneficiário só vê as próprias operações, e o ID dele sempre vem do token
- **Erros.** Sempre `{ codigo, mensagem }`; a tela trata cada código (sessão expirada, conflito, não autorizado...)
- **DynamoDB e AWS.** Escolhas conscientes: estou estudando para a certificação AWS Cloud Practitioner e usei o projeto como treino. A demonstração roda numa EC2 com Docker Compose e HTTPS (Caddy), expondo só as portas 80 e 443
- **Simulador da Central.** O comportamento não foi alterado; só corrigi os erros da extração do PDF que impediam o código de rodar (nomes como `parcelald` → `parcelaId`, chaves fora do lugar, aspas trocadas)
- **Idioma.** Código em inglês; o que é contrato (JSON, códigos de erro, status, rotas) fica em português

## 3. Bibliotecas

| Biblioteca | Por quê |
|---|---|
| **Spring Boot 4.1** (Web, Validation, Security, OAuth2 Resource Server, Actuator) | Base da API, validação dos corpos, JWT e healthcheck. A 4.1 é a versão mais recente com suporte |
| **AWS SDK v2** (`dynamodb-enhanced`) | Acesso ao DynamoDB convertendo classes Java em itens, sem montar os mapas à mão |
| **springdoc-openapi** | Swagger gerado a partir do código, sempre atualizado |
| **WireMock** | Simula a Central nos testes, inclusive o que o simulador não tem (timeout, 503 seguido de sucesso) |
| **React 19 + TypeScript** | Interface tipada a partir do contrato da API |
| **Material UI** | Componentes acessíveis prontos e um tema para a identidade visual |
| **React Router** | Rotas e proteção por perfil |
| **Cypress** | Testes E2E contra o sistema completo |
| **Docker Compose, nginx e Caddy** | Subir tudo com um comando; servir o front; HTTPS automático na publicação |

O retry usa o `@Retryable` nativo do Spring Framework 7, sem biblioteca extra. As versões estão nos READMEs do [backend](backend/README.md#bibliotecas) e do [frontend](frontend/README.md#bibliotecas).

## 4. Como usei IA

Usei o **Claude Code** no VS Code (modelo Opus, esforço médio) para gerar e refatorar código, e o **Claude** (chat) para planejamento e dúvidas. Revisei tudo antes de cada commit e testei os cenários na mão (BEN001 confirmada, BEN002 rejeitada, BEN_ERRO indisponível).

**Onde usei:**
- **Planejamento:** quebrar o desafio em etapas, escolher as versões e antecipar problemas (fuso horário nas datas, `localhost` dentro do Docker, a resposta 204 da Central)
- **Backend:** a configuração do DynamoDB Local e a autenticação (BCrypt, JWT, regras por perfil e as respostas 401/403)
- **Frontend:** a geração de IDs, a formatação de moeda e data, a sessão no `sessionStorage`, os dados simulados para desenvolver antes do backend e o favicon
- **Testes, infraestrutura e documentação:** apoio na escrita dos testes, no Docker Compose, na publicação na AWS e nos três READMEs, conferindo cada afirmação contra o código
- **Refatoração:** padronização do código em inglês

**Onde corrigi a IA:**
- **Forma de testar:** a sugestão era testar só com chamadas montadas à mão (curl); preferi o **Swagger**, que mostra as rotas, os corpos e as respostas, e por isso deixei o Swagger público na demonstração
- **Arquitetura na AWS:** a IA justificou o x86 dizendo que o DynamoDB Local talvez não tivesse versão ARM. Questionei, conferimos no Docker Hub (a imagem tem `arm64`) e o argumento foi descartado. Aprendi também que rodar no Mac não provaria isso, porque o Docker Desktop pode emular imagens `amd64`
- **Documentação:** a revisão dos READMEs encontrou textos que não batiam com o código (um código de erro antigo, um cenário de teste descrito errado, uma nota contraditória sobre o Docker Compose da AWS), que corrigi antes da entrega
- **Nomes:** ajustei nomes sugeridos que não seguiam o padrão do projeto

## 5. O que ficou de fora e o que faria a seguir

**Ficou de fora por falta de tempo:**
- **GitHub Actions** rodando os testes a cada push
- **Testes de componente no front** (os fluxos principais estão cobertos pelos 5 testes E2E)

**Faria a seguir:**
- **Logs do fluxo** com o `operacaoId` em cada passo, sem dados sensíveis
- **Nomes no Swagger**, agrupando as rotas com `@Tag`
- **Simulador estendido**, separado do oficial, com cenários que a tela hoje não mostra: envio sempre 503 com agenda preenchida, 503 seguido de sucesso, resposta mais lenta que o timeout e parcela já quitada

