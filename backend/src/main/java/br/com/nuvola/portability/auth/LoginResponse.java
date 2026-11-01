package br.com.nuvola.portability.auth;

public record LoginResponse(String token, Role perfil, String nome) {
}
