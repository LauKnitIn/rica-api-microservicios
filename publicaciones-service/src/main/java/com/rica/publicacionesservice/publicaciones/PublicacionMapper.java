package com.rica.publicacionesservice.publicaciones;


public class PublicacionMapper {
    private PublicacionMapper() {
    }

    public static Publicacion aEntidad(PublicacionRequest request) {
        Publicacion p = new Publicacion();
        String c = request.getInvestigadorCorreo();
        p.setInvestigadorCorreo(c);
        p.setTitulo(request.getTitulo());
        p.setTipo(request.getTipo());
        p.setAnio(request.getAnio());
        p.setDetalles(request.getDetalles());
        return p;
    }

    public static PublicacionResponse aResponse(Publicacion publicacion) {
        return new PublicacionResponse(
                publicacion.getInvestigadorCorreo().toString(),
                publicacion.getTitulo(),
                publicacion.getTipo(),
                publicacion.getAnio(),
                publicacion.getDetalles(),
                publicacion.getId().toString()
        );
    }
}
