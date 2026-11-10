package br.com.honorarium.honorarios_api.common;

public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() { super("Credenciais inválidas"); }
}
