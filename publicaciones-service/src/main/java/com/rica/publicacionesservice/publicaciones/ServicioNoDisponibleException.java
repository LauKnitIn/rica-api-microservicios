package com.rica.publicacionesservice.publicaciones;

public class ServicioNoDisponibleException extends RuntimeException{
    
    public ServicioNoDisponibleException(String mensaje, Throwable cause){
        super(mensaje, cause);
    }
}
