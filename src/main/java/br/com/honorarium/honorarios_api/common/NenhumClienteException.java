package br.com.honorarium.honorarios_api.common;

public class NenhumClienteException extends RuntimeException {
    public NenhumClienteException() {super("Nenhum cliente encontrado");}
}
