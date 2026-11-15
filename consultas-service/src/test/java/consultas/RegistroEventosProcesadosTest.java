package consultas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.rica.consultasservice.consultas.EventoProcesado;
import com.rica.consultasservice.consultas.RegistroEventosProcesados;

public class RegistroEventosProcesadosTest {
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final RegistroEventosProcesados registro = new RegistroEventosProcesados(mongoTemplate);

    @Test
    void laPrimeraVezDevuelveTrue() {
        assertThat(registro.primeraVez("investigadores.registrados-0-5")).isTrue();
    }

    @Test
    void laSegundaVezDevuelveFalseCuandoLaBaseRechazaElIdDuplicado() {
        doThrow(new DuplicateKeyException("duplicado")).when(mongoTemplate).insert(any(EventoProcesado.class));

        assertThat(registro.primeraVez("investigadores.registrados-0-5")).isFalse();
    }
}
