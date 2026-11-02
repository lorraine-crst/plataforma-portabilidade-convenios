describe("Login", () => {
  it("senha errada mostra 'Usuário ou senha inválidos'", () => {
    cy.visit("/login");
    cy.get("#login-username").type("operador");
    cy.get("#login-password").type("senha-errada");

    cy.contains("button", "Entrar").click();

    cy.contains("Usuário ou senha inválidos").should("be.visible");
    cy.location("pathname").should("eq", "/login");
  });
});