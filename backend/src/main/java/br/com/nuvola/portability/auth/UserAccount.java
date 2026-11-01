package br.com.nuvola.portability.auth;

public record UserAccount(String username, String passwordHash, Role role, String displayName) {
}
