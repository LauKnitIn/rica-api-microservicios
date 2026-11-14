package com.rica.publicacionesservice.publicaciones;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rica.publicacionesservice.compartido.LimiteAnualExcedidoException;
import com.rica.publicacionesservice.compartido.RecursoNoEncontradoException;



@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final VerificadorInvestigador investigadorRepository;
    private final LimitePublicacionesAnualesService limitePublicacionesService;
    private final PublicadorEventosPublicaciones publicadorEventos;

    public PublicacionService(PublicacionRepository publicacionRepository,
                               VerificadorInvestigador investigadorRepository,
                                LimitePublicacionesAnualesService limitePublicacionesAnualesService,
                                PublicadorEventosPublicaciones publicadorEventos) {
        this.publicacionRepository = publicacionRepository;
        this.investigadorRepository = investigadorRepository;
        this.limitePublicacionesService = limitePublicacionesAnualesService;
        this.publicadorEventos = publicadorEventos;
    }

    public Publicacion registrar(Publicacion publicacion) {
        if (!investigadorRepository.existe(publicacion.getInvestigadorCorreo())) {
            throw new RecursoNoEncontradoException(
                    "No existe un investigador con correo " + publicacion.getInvestigadorCorreo());
        }
        if (!limitePublicacionesService.puedeRegistrar(publicacion.getInvestigadorCorreo(), publicacion)) {
            throw new LimiteAnualExcedidoException(
                "El investigador " + publicacion.getInvestigadorCorreo() + "ya tiene 5 publicaciones registradas este año"
            );
        }
        Publicacion guardada = publicacionRepository.save(publicacion);
        publicadorEventos.publicacionRegistrada(guardada);
        return guardada;
    }

    public List<Publicacion> listarPorInvestigador(String investigadorCorreo) {
        return publicacionRepository.findByInvestigadorCorreo(investigadorCorreo);
    }

    public Publicacion buscarPorId(String id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una publicación con id " + id));
    }
}
