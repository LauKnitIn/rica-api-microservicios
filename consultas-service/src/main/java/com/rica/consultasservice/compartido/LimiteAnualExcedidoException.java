package com.rica.consultasservice.compartido;

public class LimiteAnualExcedidoException extends RuntimeException{
    public LimiteAnualExcedidoException(String mensaje) {
        super(mensaje);
    }
}
