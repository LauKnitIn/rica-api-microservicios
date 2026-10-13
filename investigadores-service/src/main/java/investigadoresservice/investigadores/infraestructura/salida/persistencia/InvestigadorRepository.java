package investigadoresservice.investigadores.infraestructura.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import investigadoresservice.investigadores.dominio.CorreoInstitucional;
import investigadoresservice.investigadores.dominio.Investigador;




public interface InvestigadorRepository extends JpaRepository<Investigador,Long>{
    boolean existsByCorreoInstitucional_Valor(String correoInstitucional);
    Investigador findByCorreoInstitucional_Valor(CorreoInstitucional valor);
}
