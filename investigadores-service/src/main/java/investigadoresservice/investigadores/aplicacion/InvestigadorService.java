package investigadoresservice.investigadores.aplicacion;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import investigadoresservice.compartido.RecursoNoEncontradoException;
import investigadoresservice.investigadores.dominio.CorreoDuplicadoException;
import investigadoresservice.investigadores.dominio.Investigador;



@Service
public class InvestigadorService implements InvestigadorUseCase {
    private final RepositorioInvestigadores investigadorRepository;
    private final InvestigadorFactory investigadorFactory;
    private final PublicadorEventos publicadorEventos;
    

    public InvestigadorService(RepositorioInvestigadores repositorioInvestigadores,
                           InvestigadorFactory investigadorFactory,
                           PublicadorEventos publicadorEventos) {
    this.investigadorRepository = repositorioInvestigadores;
    this.investigadorFactory = investigadorFactory;
    this.publicadorEventos = publicadorEventos;
}

    public List<Investigador> listarTodos() {
        return investigadorRepository.listarTodos();
    }

    public Investigador buscarPorId(Long id) {
        return investigadorRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un investigador con id " + id));
    }

   @Override
    @Transactional
    public Investigador registrar(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        Investigador investigador = investigadorFactory.crear(nombreCompleto, correoInstitucional, grupoInvestigacion);
        Investigador guardado = investigadorRepository.guardar(investigador);

        publicadorEventos.publicar(
                "investigadores.registrados",
                correoInstitucional,
                Map.of("id", guardado.getId(),
                    "correoInstitucional", correoInstitucional,
                    "nombreCompleto", nombreCompleto));
        return guardado;
    }  
}
