const PASSWORD = "lola123";
const UNAVAILABLE_MESSAGE =
  "A Central de Convênios está indisponível no momento. Tente novamente em instantes.";

function authorizeAsBeneficiary(username: string) {
  cy.intercept("GET", "/api/autorizacao").as("myAuthorization");
  cy.login(username, PASSWORD);
  cy.wait("@myAuthorization").then(({ response }) => {
    if (response?.statusCode === 404) {
      cy.contains("label", "Li e autorizo").click();
      cy.contains("button", "Autorizar").click();
      cy.get('[role="dialog"]').should("not.exist");
    }
  });
  cy.contains("button", "Sair").click();
}

function openScheduleAsOperator(beneficiaryId: string) {
  cy.login("operador", PASSWORD);
  cy.visit("/operador/portabilidade");
  cy.get("#beneficiary-id").type(beneficiaryId);
  cy.contains("button", "Consultar").click();
}

function fillPortability(installmentIds: string[]) {
  installmentIds.forEach((installmentId) => {
    cy.get(`[aria-label="Selecionar a parcela ${installmentId}"]`).check();
  });
  cy.get("#account-banco").type("001");
  cy.get("#account-agencia").type("1234");
  cy.get("#account-conta").type("56789-0");
}

describe("Fluxo de portabilidade", () => {
  it("BEN001: o beneficiário autoriza, o operador envia e a portabilidade é confirmada", () => {
    authorizeAsBeneficiary("BEN001");
    openScheduleAsOperator("BEN001");
    fillPortability(["P001", "P002"]);

    cy.contains("button", "Solicitar portabilidade").click();

    cy.location("pathname").should("match", /^\/operacoes\//);
    cy.contains("h2", "Portabilidade confirmada", { timeout: 15000 }).should(
      "be.visible",
    );
  });

  it("BEN002: o mesmo fluxo termina rejeitado, com o motivo na tela", () => {
    authorizeAsBeneficiary("BEN002");
    openScheduleAsOperator("BEN002");
    fillPortability(["P010"]);

    cy.contains("button", "Solicitar portabilidade").click();

    cy.contains("h2", "Portabilidade rejeitada", { timeout: 15000 }).should(
      "be.visible",
    );
    cy.contains("Beneficiário atingiu o limite de margem consignável").should(
      "be.visible",
    );
  });

  it("duplo clique em Solicitar gera uma única requisição", () => {
    authorizeAsBeneficiary("BEN001");
    openScheduleAsOperator("BEN001");
    fillPortability(["P001"]);
    cy.intercept("POST", "/api/portabilidades").as("createPortability");

    cy.contains("button", "Solicitar portabilidade").dblclick();

    cy.wait("@createPortability");
    cy.location("pathname").should("match", /^\/operacoes\//);
    cy.get("@createPortability.all").should("have.length", 1);
  });

  it("Central fora do ar: mostra a mensagem e o Tentar novamente reenvia a mesma Idempotency-Key", () => {
    authorizeAsBeneficiary("BEN001");
    openScheduleAsOperator("BEN001");
    fillPortability(["P002"]);
    cy.intercept("POST", "/api/portabilidades").as("retry");
    cy.intercept(
      { method: "POST", url: "/api/portabilidades", times: 1 },
      {
        statusCode: 503,
        body: { codigo: "CENTRAL_INDISPONIVEL", mensagem: UNAVAILABLE_MESSAGE },
      },
    ).as("unavailable");

    cy.contains("button", "Solicitar portabilidade").click();

    cy.wait("@unavailable")
      .its("request.headers")
      .its("idempotency-key")
      .as("firstKey");
    cy.contains(UNAVAILABLE_MESSAGE).should("be.visible");

    cy.contains("button", "Tentar novamente").click();

    cy.get("@firstKey").then((firstKey) => {
      cy.wait("@retry")
        .its("request.headers")
        .its("idempotency-key")
        .should("eq", firstKey);
    });
    cy.location("pathname").should("match", /^\/operacoes\//);
  });
});