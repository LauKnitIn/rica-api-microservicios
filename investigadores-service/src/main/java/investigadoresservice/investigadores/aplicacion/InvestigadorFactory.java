package investigadoresservice.investigadores.aplicacion;

import org.springframework.stereotype.Component;

import investigadoresservice.investigadores.dominio.CorreoInstitucional;
import investigadoresservice.investigadores.dominio.Investigador;


@Component
public class InvestigadorFactory {
    private final RepositorioInvestigadores investigadorRepository;

    public InvestigadorFactory(RepositorioInvestigadores investigadorRepository) {
        this.investigadorRepository = investigadorRepository;
    }      
    
    public Investigador crear(String nombreCompleto, String correoInstitucional, String grupoInvestigacion) {
        CorreoInstitucional correo = new CorreoInstitucional(correoInstitucional);
        if (investigadorRepository.existeCorreo(correoInstitucional)) {
            throw new IllegalArgumentException("Ya existe un investigador con el correo " + correo);
        }
        return new Investigador(null, nombreCompleto, correo, grupoInvestigacion);
    }
}
