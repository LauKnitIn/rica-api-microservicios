package rica.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import investigadoresservice.investigadores.aplicacion.InvestigadorFactory;
import investigadoresservice.investigadores.aplicacion.InvestigadorService;
import investigadoresservice.investigadores.aplicacion.PublicadorEventos;
import investigadoresservice.investigadores.dominio.Investigador;
import static org.assertj.core.api.Assertions.assertThat;

public class InvestigadorServiceConFalsoTest {

    @Mock
    private PublicadorEventos publicadorEventos;

    @Test
    void registraYRecuperaUnInvestigadorSinNingunaDependenciaDeSpringNiDeBaseDeDatos() {
        RepositorioInvestigadoresFalso repositorio = new RepositorioInvestigadoresFalso();
        InvestigadorFactory factory = new InvestigadorFactory(repositorio);
        InvestigadorService service = new InvestigadorService(repositorio, factory, publicadorEventos);

        Investigador guardado = service.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(guardado.getId()).isNotNull();
        assertThat(service.buscarPorId(guardado.getId()).getNombreCompleto()).isEqualTo("Ana Torres");
    }

}   
