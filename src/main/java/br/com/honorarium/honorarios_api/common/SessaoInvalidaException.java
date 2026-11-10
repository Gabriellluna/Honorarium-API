package br.com.honorarium.honorarios_api.common;

public class SessaoInvalidaException extends RuntimeException {
    public SessaoInvalidaException() { super("Sessão inválida"); }
}
