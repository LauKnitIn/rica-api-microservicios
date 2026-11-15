package com.rica.publicacionesservice.publicaciones;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PublicacionServiceTest {
    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private VerificadorInvestigador verificadorInvestigador;

    @Mock
    private LimitePublicacionesAnualesService limitePublicacionesAnualesService;

    @Mock
    private PublicadorEventosPublicaciones publicadorEventos;

    @InjectMocks
    private PublicacionService publicacionService;

    private Publicacion nuevaPublicacion() {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        publicacion.setTitulo("Arquitecturas evolutivas");
        publicacion.setTipo("articulo");
        publicacion.setAnio(2026);
        publicacion.setDetalles(Map.of());
        return publicacion;
    }

    @Test
    void registrarPublicaElEventoDespuesDeGuardar() {
        Publicacion entrada = nuevaPublicacion();
        Publicacion guardada = new Publicacion();
        guardada.setId("abc123");
        guardada.setInvestigadorCorreo("ana.torres@uptc.edu.co");
        guardada.setTitulo("Arquitecturas evolutivas");
        guardada.setTipo("articulo");
        guardada.setAnio(2026);
        guardada.setDetalles(Map.of());
        when(verificadorInvestigador.existe("ana.torres@uptc.edu.co")).thenReturn(true);
        when(limitePublicacionesAnualesService.puedeRegistrar("ana.torres@uptc.edu.co", entrada)).thenReturn(true);
        when(publicacionRepository.save(entrada)).thenReturn(guardada);

        Publicacion resultado = publicacionService.registrar(entrada);

        assertThat(resultado.getId()).isEqualTo("abc123");
        verify(publicadorEventos).publicacionRegistrada(guardada);
    }

    @Test
    void registrarNoPublicaEventoSiElInvestigadorNoExiste() {
        when(verificadorInvestigador.existe("nadie@uptc.edu.co")).thenReturn(false);
        Publicacion entrada = new Publicacion();
        entrada.setAnio(2026);
        entrada.setInvestigadorCorreo("nadie@uptc.edu.co");
        entrada.setTitulo("X");
        entrada.setTipo("libro");
        entrada.setAnio(2026);
        entrada.setDetalles(Map.of());

        try {
            publicacionService.registrar(entrada);
        } catch (RuntimeException esperado) {
        }

        verify(publicadorEventos, never()).publicacionRegistrada(any());
    }
}
