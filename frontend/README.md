# Frontend — Nuvola

Interface da plataforma de portabilidade de convênios, em **React 19**, **TypeScript** e **Material UI**. Atende os dois perfis: o **beneficiário** (autoriza e acompanha as próprias solicitações) e o **operador** (consulta a agenda, pede a portabilidade e acompanha o painel).

← [Voltar ao README principal](../README.md) · Contrato da API: [`docs/api.md`](../docs/api.md)

## Como rodar só o front

Requer Node 24 (ver `.nvmrc`) e o backend rodando em `localhost:8080`. Dentro de `frontend/`:

```bash
npm install
npm run dev
```

O front sobe em http://localhost:3000. O servidor de desenvolvimento repassa as chamadas `/api` para o backend (proxy do webpack), então o navegador fala só com um endereço e não há problema de CORS.

| Script | O que faz |
|---|---|
| `npm run dev` | Servidor de desenvolvimento, porta 3000 |
| `npm run build` | Build de produção na pasta `dist/` |
| `npm run typecheck` | Confere os tipos do TypeScript |
| `npm run format` / `format:check` | Formata / confere a formatação com o Prettier |
| `npm run e2e` / `e2e:open` | Testes E2E no terminal / com a interface do Cypress |

No Docker Compose, o front é servido pelo **nginx**, que entrega os arquivos do `dist/` e repassa `/api` para o backend. O `try_files` faz o F5 funcionar em qualquer rota.

## Telas

| Tela | Rota | Perfil |
|---|---|---|
| Login | `/login` | — |
| Minhas solicitações | `/beneficiario/solicitacoes` | Beneficiário: autorização (opt-in), resumo e lista das próprias solicitações |
| Nova portabilidade | `/operador/portabilidade` | Operador: consultar o beneficiário → selecionar as parcelas → conta de destino |
| Painel | `/operador/painel` | Operador: resumo e lista de todas as solicitações, com filtros |
| Acompanhamento | `/operacoes/:id` | Os dois: status, histórico e resultado da operação |

## Estrutura

```
src/
├── api/            cliente HTTP único, chamadas da API e o ApiError
├── auth/           sessão, rotas protegidas e perfis
├── components/     peças reutilizáveis (alertas, status, carregando, página de erro)
├── hooks/          useFetch (carregando, erro e recarregar)
├── notifications/  avisos rápidos (snackbar)
├── pages/
│   ├── beneficiary/  minhas solicitações e o dialog de autorização
│   ├── operator/     nova portabilidade e painel
│   └── shared/       login, acompanhamento, resumo e tabela de solicitações
├── types/          os tipos do contrato da API
└── utils/          datas, moeda, validação e geração de IDs
```

## Decisões técnicas

### Comunicação com a API
- **Um cliente HTTP só** (`api/client.ts`): coloca o token, define o timeout e trata os erros num lugar. Nenhuma tela chama o `fetch` direto
- **Timeout de 10 s** (com `AbortController`): uma requisição que não responde vira erro com mensagem, em vez de deixar a tela carregando para sempre
- **Erro fora do formato `{ codigo, mensagem }`** (por exemplo, o backend desligado) vira `SEM_CONEXAO`, um código que só o front gera
- **Os tipos do contrato** ficam em `types/index.ts`, espelhando o `docs/api.md`

### Sessão e rotas
- A sessão fica no `sessionStorage`: sobrevive ao F5, mas não a fechar a aba, e não é compartilhada entre abas. Isso reduz a exposição do token em relação ao `localStorage`, mas os dois podem ser lidos por JavaScript da página; a proteção contra XSS vem do React, que escapa os textos exibidos
- **`SESSAO_EXPIRADA` tratado num ponto só:** o `client.ts` avisa o `AuthProvider`, que faz logout e volta para o login com o aviso "Sessão expirada". Nenhuma tela precisa se preocupar com isso
- **As rotas protegidas por perfil são uma proteção de interface.** A autorização de verdade é do backend

### Como a duplicidade é evitada
Duas proteções para dois problemas diferentes:
1. **Duplo clique:** uma trava num `useRef`, que muda na hora e ignora o segundo clique. O `useState` sozinho não basta, porque a atualização só acontece no próximo render, e dois cliques rápidos acontecem antes dele.
2. **Reenvio depois de falha de rede:** cada pedido leva o header `Idempotency-Key`, um ID aleatório (`generateId`, com uma alternativa para quando o `crypto.randomUUID()` não está disponível). O front guarda a chave junto com o conteúdo do pedido (o beneficiário, as parcelas ordenadas e a conta): se o pedido for reenviado igual, a chave é reaproveitada; se qualquer coisa mudar, uma chave nova é gerada. Se a primeira requisição chegou ao backend mas a resposta se perdeu, o "Tentar novamente" manda a mesma chave e recebe a operação que já existe.

A garantia final é do backend (ver o [README do backend](../backend/README.md#como-a-duplicidade-é-evitada)).

### Erros na tela

| Código | O que a tela faz |
|---|---|
| `CREDENCIAIS_INVALIDAS` | Mostra "Usuário ou senha inválidos" no login |
| `SESSAO_EXPIRADA` | Logout e volta para o login com o aviso "Sessão expirada" |
| `NAO_ENCONTRADO` | No acompanhamento: tela "Solicitação não encontrada". Na autorização: entende que o beneficiário ainda não autorizou e abre o dialog do termo |
| `CONFLITO` | Tela "Esta solicitação já foi enviada com outros dados" |
| `BENEFICIARIO_NAO_AUTORIZADO` | Aviso informativo: o beneficiário precisa autorizar antes |
| Demais códigos | Alerta com a `mensagem` do backend e um botão para tentar de novo |

### Espera pela Central
- A confirmação não é imediata (cerca de 5 s no simulador). O acompanhamento consulta o status a cada **2 s** enquanto ele está `PENDENTE`
- Depois de **60 s** sem resposta, a atualização automática pausa, com um aviso e o botão "Atualizar agora", para não ficar consultando para sempre
- O resultado aparece em destaque: "Portabilidade confirmada", com o total e as parcelas, ou "Portabilidade rejeitada", com o motivo
- Um status fora dos três conhecidos mostra um aviso, em vez de quebrar a tela

### Datas e valores
- A API trabalha com datas em ISO; a formatação acontece só na tela, em pt-BR (`Intl`)
- **Datas de calendário** (como a data de desconto, `2026-01-10`) são formatadas a partir do texto, sem passar por `new Date()`. Assim, o fuso horário do navegador não desloca o dia
- **Data e hora** (como `criadaEm`) aparecem no horário local do usuário
- Valores em reais com `Intl.NumberFormat` (`R$ 150,00`)
- **Parcelas com data passada:** aparecem com a marca "Data já passou", e um aviso explica que podem já ter sido descontadas pelo credor atual e que a Central decide se ainda podem ser portadas. A seleção depende só do status: qualquer parcela `ABERTA` pode ser escolhida

### Painel e resumo
- Os cartões do resumo funcionam como **filtro**: clicar em "Aceitas" mostra só as confirmadas; clicar de novo limpa o filtro
- O operador também filtra por beneficiário; o beneficiário vê só as próprias solicitações (o backend garante)
- O botão "Atualizar" recarrega o resumo e a lista

### Acessibilidade e identidade visual
- Link "Pular para o conteúdo" no topo, para quem navega pelo teclado
- Rótulos de acessibilidade nos checkboxes das parcelas e nos botões de ícone; regiões `aria-live` para anunciar mudanças de status e de seleção
- Cartões de filtro com `aria-pressed`, para o leitor de tela saber qual está ativo
- Identidade visual própria da Nuvola (logo, paleta e imagem do login), com a fonte Roboto servida junto com o app, sem depender de CDN

## Testes E2E

Com tudo rodando (`docker compose up --build -d`, na raiz do repositório), dentro de `frontend/`:

```bash
npm run e2e
```

Use `npm run e2e:open` para acompanhar no navegador. São 5 testes contra o sistema de verdade (front, backend, simulador e banco):

| Teste | O que prova |
|---|---|
| BEN001 | O fluxo completo: autorização, agenda, envio e confirmação |
| BEN002 | A rejeição, com o motivo na tela |
| Duplo clique | Dois cliques rápidos em "Solicitar" geram uma única requisição |
| Central fora do ar | A mensagem de erro aparece, e o "Tentar novamente" reenvia a mesma `Idempotency-Key` (o 503 é simulado com `cy.intercept`, porque a agenda do `BEN_ERRO` no simulador é vazia) |
| Login inválido | A senha errada mostra "Usuário ou senha inválidos" |

- **Seletores:** os testes usam os `id`, os rótulos de acessibilidade (`aria-label`) e os textos que a tela já tem, sem `data-testid`. Eles encontram os elementos como um usuário e um leitor de tela encontrariam, e o código do front não precisou mudar
- **Banco não zerado:** os testes funcionam com o banco já usado e em qualquer ordem; a autorização só é aceita quando o `GET /api/autorizacao` responde 404
- **Contra a demonstração:** `npx cypress run --config baseUrl=https://54-20-185-96.sslip.io`

## Bibliotecas

| Biblioteca | Versão | Para quê |
|---|---|---|
| React | 19.3.1 | Interface |
| TypeScript | 6.0.3 | Tipagem, inclusive do contrato da API |
| MUI (Material UI) + Emotion | 9.5.0 | Componentes acessíveis e o tema da identidade visual |
| React Router | 8.5.1 | Rotas e proteção por perfil |
| Roboto (`@fontsource/roboto`) | 5.x | Fonte servida junto com o app |
| webpack | 5.112.2 | Build e servidor de desenvolvimento, com proxy de `/api` |
| Cypress | 16.2.1 | Testes E2E |
| Prettier | 3.9.10 | Formatação do código |