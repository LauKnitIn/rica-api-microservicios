package rica.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import investigadoresservice.compartido.RecursoNoEncontradoException;
import investigadoresservice.investigadores.aplicacion.InvestigadorFactory;
import investigadoresservice.investigadores.aplicacion.InvestigadorService;
import investigadoresservice.investigadores.aplicacion.PublicadorEventos;
import investigadoresservice.investigadores.aplicacion.RepositorioInvestigadores;
import investigadoresservice.investigadores.dominio.CorreoDuplicadoException;
import investigadoresservice.investigadores.dominio.CorreoInstitucional;
import investigadoresservice.investigadores.dominio.Investigador;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvestigadorServiceTest {
    @Mock
    private RepositorioInvestigadores investigadorRepository;

    @Mock
    private InvestigadorFactory investigadorFactory;

    @InjectMocks
    private InvestigadorService investigadorService;

    @Mock
    private PublicadorEventos publicadorEventos;

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional( "ana.torres@uptc.edu.co"), "GIT-UPTC");
        when(investigadorRepository.buscarPorId(1L)).thenReturn(Optional.of(investigador));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(investigadorRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigadorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        Investigador nuevo = new Investigador(null, "Carlos Ruiz", new CorreoInstitucional("carlos.ruiz@uptc.edu.co"), "GIT-UPTC");
    
        when(investigadorFactory.crear("Carlos Ruiz", "carlos.ruiz@uptc.edu.co", "GIT-UPTC")).thenReturn(nuevo);
    
        when(investigadorRepository.existeCorreo("carlos.ruiz@uptc.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> investigadorService.registrar(nuevo.getNombreCompleto(), nuevo.getCorreoInstitucional().valor(), nuevo.getGrupoInvestigacion()))
            .isInstanceOf(CorreoDuplicadoException.class);

        verify(investigadorRepository).existeCorreo("carlos.ruiz@uptc.edu.co");
    }
    
    @Test
    void confirmarEliminacionBorraAlInvestigadorYPublicaElEventoEliminado() {
        Investigador enEliminacion = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        enEliminacion.marcarEnEliminacion();
        when(investigadorRepository.buscarPorCorreo("ana.torres@uptc.edu.co")).thenReturn(Optional.of(enEliminacion));

        investigadorService.confirmarEliminacion("ana.torres@uptc.edu.co");

        verify(investigadorRepository).eliminar(enEliminacion);
        verify(publicadorEventos).publicar(
                "investigadores.eliminados",
                "ana.torres@uptc.edu.co",
                java.util.Map.of("correoInstitucional", "ana.torres@uptc.edu.co"));
    }
}
