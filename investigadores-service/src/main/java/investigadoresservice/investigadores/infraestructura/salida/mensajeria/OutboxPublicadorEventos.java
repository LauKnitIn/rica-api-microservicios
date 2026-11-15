package investigadoresservice.investigadores.infraestructura.salida.mensajeria;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import investigadoresservice.investigadores.aplicacion.PublicadorEventos;
import tools.jackson.databind.ObjectMapper;

@Component 
public class OutboxPublicadorEventos implements PublicadorEventos {

    private final OutboxEventRepository repositorio;
    private final ObjectMapper objectMapper;

    public OutboxPublicadorEventos(OutboxEventRepository repositorio, ObjectMapper objectMapper) {
        this.repositorio = repositorio;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publicar(String topico, String clave, Map<String, Object> contenido) {
        Map<String, Object> conTraza = new HashMap<>(contenido);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            conTraza.put("correlationId", correlationId);
        }
        String payload = objectMapper.writeValueAsString(conTraza);
        repositorio.save(new OutboxEvent(topico, clave, payload));
    }
}
