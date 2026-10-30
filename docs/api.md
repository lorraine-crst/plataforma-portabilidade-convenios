# Contrato da API — Nuvola

O front chama `/api/...` e o dev server repassa para o backend em `localhost:8080`.
O front nunca chama a Central de Convênios diretamente.

## Convenções

- Requisições e respostas em JSON (`Content-Type: application/json`)
- Todas as rotas, exceto o login, exigem `Authorization: Bearer <token>`
- Datas de calendário no formato `AAAA-MM-DD` (ex.: `2026-01-10`); o front exibe como `dd/mm/aaaa`
- Data e hora em ISO 8601 UTC (ex.: `2026-10-29T21:40:00Z`); o front exibe no horário local do usuário
- Valores em reais como número (ex.: `150.00`)
- Perfis: `BENEFICIARIO`, `OPERADOR`
- Status da parcela: o que vier da Central (ex.: `ABERTA`)
- Status da operação: `PENDENTE`, `CONFIRMADA`, `REJEITADA`

## Autenticação

### POST /api/auth/login

Acesso: sem token (é a rota que gera o token)

Corpo:

```json
{ "usuario": "BEN001", "senha": "lola123" }
```

Resposta `200`:

```json
{ "token": "eyJhbGciOi...", "perfil": "BENEFICIARIO", "nome": "Lorraine Cristina" }
```

## Autorização (opt-in)

Cada beneficiário tem no máximo uma autorização. Ela é um recurso único, identificado pelo token: a mesma URL serve para consultar (`GET`) e para registrar (`POST`).

### GET /api/autorizacao

Acesso: `BENEFICIARIO`. O ID do beneficiário vem do token.

Resposta `200` quando o beneficiário já autorizou:

```json
{ "autorizacaoId": "7f1c2e9a-...", "autorizadaEm": "2026-10-29T21:40:00Z" }
```

Resposta `404` com o código `NAO_ENCONTRADO` quando ainda não existe autorização.

### POST /api/autorizacao

Acesso: `BENEFICIARIO`. O ID do beneficiário vem do token, por isso não há corpo.

Resposta `201`:

```json
{ "autorizacaoId": "7f1c2e9a-...", "autorizadaEm": "2026-10-29T21:40:00Z" }
```

Se o beneficiário já tiver autorizado, a resposta é `200` com a autorização existente.

## Agenda

### GET /api/beneficiarios/{id}/agenda

Acesso: `OPERADOR`

Resposta `200`:

```json
[
  {
    "parcelaId": "P001",
    "convenioId": "CONV01",
    "valor": 150.00,
    "dataDesconto": "2026-01-10",
    "status": "ABERTA"
  }
]
```

Agenda vazia: `200` com `[]`.

## Portabilidade

### POST /api/portabilidades

Acesso: `OPERADOR`

Header obrigatório: `Idempotency-Key: <uuid>`

Corpo:

```json
{
  "beneficiarioId": "BEN001",
  "parcelaIds": ["P001", "P002"],
  "contaDestino": { "banco": "001", "agencia": "1234", "conta": "56789-0" }
}
```

Resposta `202`:

```json
{ "operacaoId": "c3d4e5f6-...", "status": "PENDENTE" }
```

Uma nova requisição com a mesma `Idempotency-Key` não cria outra operação: a resposta traz o mesmo `operacaoId`.

### GET /api/portabilidades/{id}

Acesso: `OPERADOR`, ou o `BENEFICIARIO` dono da operação

Resposta `200`:

```json
{
  "operacaoId": "c3d4e5f6-...",
  "beneficiarioId": "BEN002",
  "status": "REJEITADA",
  "motivoRejeicao": "Beneficiário atingiu o limite de margem consignável",
  "criadaEm": "2026-10-29T21:45:00Z",
  "valorTotal": 300.00,
  "parcelas": [
    {
      "parcelaId": "P010",
      "convenioId": "CONV02",
      "valor": 300.00,
      "dataDesconto": "2026-01-15",
      "status": "ABERTA"
    }
  ],
  "contaDestino": { "banco": "001", "agencia": "1234", "conta": "56789-0" },
  "eventos": [
    { "status": "PENDENTE", "ocorridoEm": "2026-10-29T21:45:00Z", "descricao": "Solicitação enviada à Central" },
    { "status": "REJEITADA", "ocorridoEm": "2026-10-29T21:45:05Z", "descricao": "Rejeitada pela Central" }
  ]
}
```

`motivoRejeicao` é `null` quando o status não é `REJEITADA`.

### GET /api/portabilidades?status=&beneficiarioId=

Acesso: `OPERADOR` e `BENEFICIARIO`. Os dois filtros são opcionais.
Para o beneficiário, o backend devolve só as operações dele e ignora `beneficiarioId`.

Resposta `200`:

```json
[
  {
    "operacaoId": "c3d4e5f6-...",
    "beneficiarioId": "BEN001",
    "status": "CONFIRMADA",
    "motivoRejeicao": null,
    "criadaEm": "2026-10-29T21:45:00Z",
    "quantidadeParcelas": 2,
    "valorTotal": 300.00
  }
]
```

### GET /api/portabilidades/resumo

Acesso: `OPERADOR` e `BENEFICIARIO`. Para o beneficiário, conta só as operações dele.

Resposta `200`:

```json
{ "total": 3, "pendentes": 1, "confirmadas": 1, "rejeitadas": 1, "valorTotalConfirmado": 300.00 }
```

## Erros

Toda resposta de erro tem o mesmo formato:

```json
{ "codigo": "CENTRAL_INDISPONIVEL", "mensagem": "Central indisponível no momento. Tente novamente." }
```

- `codigo`: fixo, usado pelo front para decidir o que fazer
- `mensagem`: texto em português, pronto para mostrar ao usuário
- Erro fora do formato `{ codigo, mensagem }` (ex.: backend desligado): o front trata como `SEM_CONEXAO`

| Código | HTTP | Quando acontece |
|---|---|---|
| `CREDENCIAIS_INVALIDAS` | 401 | Usuário ou senha incorretos no login |
| `SESSAO_EXPIRADA` | 401 | Token ausente, inválido ou expirado em qualquer rota protegida |
| `ACESSO_NEGADO` | 403 | Perfil sem permissão para a rota, ou beneficiário tentando ver operação de outro |
| `NAO_ENCONTRADO` | 404 | Operação inexistente em `GET /api/portabilidades/{id}`, ou beneficiário sem autorização em `GET /api/autorizacao` |
| `CONFLITO` | 409 | Mesma `Idempotency-Key` enviada com corpo diferente |
| `BENEFICIARIO_NAO_AUTORIZADO` | 422 | Operador consulta agenda ou solicita portabilidade de beneficiário sem opt-in |
| `CENTRAL_INDISPONIVEL` | 503 | A Central não respondeu ou respondeu com erro (ex.: `BEN_ERRO`) |
| `VALIDACAO` | 400 | Corpo inválido, campo obrigatório ausente ou `Idempotency-Key` ausente |
| `SEM_CONEXAO` | — | Gerado pelo próprio front quando a requisição nem chega ao backend |

Comportamento do front por código:

- `SESSAO_EXPIRADA`: faz logout e redireciona para `/login` com o aviso "Sessão expirada"
- `NAO_ENCONTRADO`: em `GET /api/portabilidades/{id}`, mostra a tela de erro com "Solicitação não encontrada"; em `GET /api/autorizacao`, indica que o beneficiário ainda não autorizou e abre o dialog de autorização
- `CONFLITO`: mostra a tela de erro com "Esta solicitação já foi enviada com outros dados"
- Demais códigos: a própria tela mostra um `<AppAlert type="error">` com a `mensagem`

## Usuários de teste

Todos usam a senha `lola123`.

| Usuário | Perfil | Nome exibido |
|---|---|---|
| `operador` | `OPERADOR` | Operador Nuvola |
| `BEN001` | `BENEFICIARIO` | Lorraine Cristina |
| `BEN002` | `BENEFICIARIO` | Ada Lovelace |
| `BEN_ERRO` | `BENEFICIARIO` | Santos Dumont |

Nenhum beneficiário começa com opt-in. Cada um precisa autorizar antes que o operador consulte a agenda.

## Cenários

| Beneficiário | Agenda na Central | Resultado da portabilidade |
|---|---|---|
| `BEN001` | 2 parcelas (`P001`, `P002`) | `CONFIRMADA` após cerca de 5 s |
| `BEN002` | 1 parcela (`P010`) | `REJEITADA` após cerca de 5 s, com o motivo "Beneficiário atingiu o limite de margem consignável" |
| `BEN_ERRO` | vazia | Pela tela, mostra "Nenhuma parcela encontrada". A Central responde 503 no POST da portabilidade, o que o backend converte em `CENTRAL_INDISPONIVEL` |
| Qualquer ID sem opt-in (ex.: `BEN003`) | — | `BENEFICIARIO_NAO_AUTORIZADO` (422) na consulta da agenda |

O simulador da Central é usado sem alterações. Como a agenda do `BEN_ERRO` vem vazia, a tela não permite enviar uma portabilidade para ele. Por isso, o erro 503 é testado em duas camadas:

- Backend: teste de integração que chama a Central com `BEN_ERRO` e verifica a resposta `503` com `CENTRAL_INDISPONIVEL`
- Front: teste de componente com `cy.intercept` devolvendo `503`, verificando que a tela mostra a mensagem e o botão "Tentar novamente"
