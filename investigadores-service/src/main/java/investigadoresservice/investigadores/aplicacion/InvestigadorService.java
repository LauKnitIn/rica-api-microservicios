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
        if (investigadorRepository.existeCorreo(correoInstitucional)) {
            throw new CorreoDuplicadoException("El correo institucional ya está registrado: " + correoInstitucional);
        }
        
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

    @Override
    @Transactional
    public void iniciarEliminacion(Long id) {
        Investigador investigador = buscarPorId(id);
        if (investigador.estaEnEliminacion()) {
            return; 
        }
        investigador.marcarEnEliminacion();
        investigadorRepository.guardar(investigador);

        String correo = investigador.getCorreoInstitucional().valor();
        publicadorEventos.publicar(
                "investigadores.en-eliminacion",
                correo,
                Map.of("correoInstitucional", correo));
    }

    @Override
    @Transactional
    public void confirmarEliminacion(String correoInstitucional) {
        investigadorRepository.buscarPorCorreo(correoInstitucional)
                .filter(Investigador::estaEnEliminacion)
                .ifPresent(investigador -> {
                    investigadorRepository.eliminar(investigador);
                    publicadorEventos.publicar(
                            "investigadores.eliminados",
                            correoInstitucional,
                            Map.of("correoInstitucional", correoInstitucional));
                });
    }
}
