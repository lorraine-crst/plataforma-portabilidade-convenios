declare global {
  namespace Cypress {
    interface Chainable {
      login(username: string, password: string): Chainable<void>;
    }
  }
}

Cypress.Commands.add("login", (username: string, password: string) => {
  cy.visit("/login");
  cy.get("#login-username").type(username);
  cy.get("#login-password").type(password, { log: false });
  cy.contains("button", "Entrar").click();
  cy.location("pathname").should("not.eq", "/login");
});

export {};