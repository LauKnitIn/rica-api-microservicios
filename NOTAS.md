# Mini-ADR: límite del agregado Investigador

## Estado

El agregado `Investigador` hoy encapsula la identidad y los datos del investigador como una entidad autónoma, pero no incluye a `Publicacion` dentro de su frontera.

## Decisión

`Publicacion` no está dentro del límite de `Investigador` porque es otro agregado: tiene su propia identidad (`id`), se persiste en una colección MongoDB (`publicaciones`) y su ciclo de vida se maneja desde `PublicacionService` con referencia al investigador por correo institucional. El límite del agregado no es “todo lo relacionado con un investigador”, sino la entidad que define su identidad y sus invariantes: nombre completo, correo institucional único y válido con dominio `@uptc.edu.co`, y grupo de investigación. La regla de negocio del año y el número máximo de publicaciones se valida fuera del agregado, desde `LimitePublicacionesAnualesService` y `PublicacionService`, usando el correo del investigador como clave de asociación.

Si alguien añadiera directamente `List<Publicacion> publicaciones` dentro de `Investigador`, rompería el límite del agregado: `Investigador` dejaría de ser la raíz del agregado y pasaría a ser un contenedor de otra entidad con ciclo de vida propio. Eso introduciría acoplamiento oculto, duplicidad de fuente de verdad, y riesgo de inconsistencia entre la base relacional de investigadores y la colección de publicaciones; además, la regla de “máximo 5 publicaciones al año” quedaría mezclada con el agregado equivocado y la entidad perdería su cohesión.

## Qué vive dentro del límite

Dentro del agregado `Investigador` viven sus atributos propios y sus invariantes: identidad, nombre completo, correo institucional, grupo de investigación, y la garantía de que no haya dos investigadores con el mismo correo institucional. Fuera del límite vive `Publicacion`, porque su mismo concepto de ciclo de vida, persistencia y validación anual pertenece a otro agregado.

## Raíz del agregado

La raíz del agregado `Investigador` es la entidad `Investigador` misma, con su identidad principal en `id` y su identidad de negocio en `correoInstitucional`. La razón de esta raíz es que la unicidad del investigador y la validación de su correo son invariantes del agregado; la publicación no pertenece a esa raíz porque se registra y consulta como entidad independiente según el correo del investigador, no como parte embebida del investigador.
 # Taller Hexagonal
1. InvestigadorRepository es un puerto secundario porque es el nucleo quien inicia la llamada a la base de datos para guardar u obtener informacion
2. Investigador controller es un adaptador primario porque recibe las peticiones desde afuera y llama al caso de uso en el nucleo, envuelve la técnologia de spring boot usando anotaciones de framework
3. Faltaría definir una interfaz en el nucleo para exponer ese contrato a los adaptadores primarios y así desacoplar el nucleo del framework.
4. Es del adaptador no del nucleo, porque a pesar de manejar lógica de construccion de Investigadores tiene la anotación de framework @Component
# Taller Microservicios
### Mapeo de componentes a servicios
Como parte de la transición hacia una arquitectura de microservicios (patrón *Database per Service*), se realizó la descomposición del monolito modular `rica-api` en dos proyectos independientes:

| Paquete / Clase actual | Destino / Decisión |
| :--- | :--- |
| `investigadores.*` (dominio, aplicación, infraestructura) | Se migra a **`investigadores-service`** |
| `publicaciones.*` | Se migra a **`publicaciones-service`** |
| `compartido.GlobalExceptionHandler`, `RecursoNoEncontradoException` | Se duplica en ambos servicios, ya que cada uno necesita su propio manejo de errores al no compartir un único `@ControllerAdvice`. *(Nota: A futuro, esto requeriría un shared kernel o librería común, pero para este taller la duplicación es la decisión inicial de arquitectura).* |
| `plataforma.StatusController`, `CorsConfig` | Se duplica en ambos servicios, adaptado para que cada uno exponga su propio endpoint `/api/status`. |
| `plataforma.ArranqueInformativo`, `SaludoInstitucionalService` | Se descartan por completo, ya que eran demostraciones iniciales de IoC/DI sin valor de negocio real. |

### Corte de la dependencia en memoria (`VerificadorInvestigador`)
Para desacoplar `publicaciones-service` de la base de datos de investigadores, se eliminó la inyección directa de `InvestigadorRepository` dentro de `PublicacionService`. En su lugar, se diseñó un puerto secundario llamado `VerificadorInvestigador` y se implementó temporalmente un adaptador (`VerificadorInvestigadorPendiente`) para permitir que el sistema compile y corra, dejando preparada la estructura para la futura comunicación por red en la siguiente lección.

# Taller CQRS

1. **La respuesta JSON de la ficha del investigador:** 
   En lugar de consultar un único documento ya listo, el sistema tendría que realizar hacer muchos joins o subconsultas en tiempo real entre la base de datos relacional de investigadores y la colección de publicaciones.
2. **El ranking por publicaciones:** 
   El cálculo del ranking necesitaria que se hicieran operaciones de agregación y conteo sobre las tablas transaccionales activas, lo que  puede afectar el rendimiento al procesamiento de comandos de escritura.
3. **El log con las líneas de proyección:** 
   No existirían el servicio de consultas ni los registros de proyección en el log, ya que no habría un consumidor de eventos de Kafka actualizando un modelo separado.
4. **La consulta de la ficha eliminada (404):** 
   La verificación de la eliminación dependería de consultar directamente las tablas relacionales transaccionales y no de la destrucción asíncrona de una ficha de lectura mediante una Saga.
5. **La lista de fichas reconstruida tras borrar la base de datos:** 
   Sería imposible "reconstruir" la base de datos de lectura eliminándola y volviéndola a poblar desde los eventos de Kafka, porque los datos solo existirían en el esquema transaccional principal.

## Taller EDA

1. **¿Quién debería enterarse cuando llega un mensaje a la DLT y cómo (alerta, tablero, correo)?**
   El equipo de desarrollo u operaciones debería enterarse con alertas automatizadas en un sistema de monitoreo o tableros (como Prometheus y Grafana), o notificaciones críticas por canales centralizados (como Slack o correo), ya que un mensaje en la DLT indica un fallo que requiere atención humana o corrección de datos.

2. **¿Cómo se reprocesaría un mensaje después de corregir el error: reenviándolo al tópico original, o con una herramienta aparte?**
   Lo ideal es utilizar una herramienta de gestión que lea el mensaje desde la DLT, permita corregir el contenido o la causa raíz del error, y lo reenvíe de forma segura al tópico original para que el consumidor regular lo procese de nuevo.

3. **¿Cuándo es válido descartar un mensaje de la DLT, y quién lo decide?**
   Es válido descartarlo únicamente cuando se ratifico que el mensaje es basura obsoleta, spam o que su pérdida no afecta la integridad del negocio esta decisión debe ser tomada y validada conjuntamente por un desarrollador senior o el arquitecto responsable del dominio.

4. **¿Qué pasa si el mensaje de la DLT es válido pero llegó a destiempo?**
   Si el mensaje es estructuralmente válido pero llegó fuera de orden o destiempo  el consumidor puede fallar al intentar aplicarlo. por ello, la lógica de reprocesamiento debe asegurar que las dependencias temporales estén resueltas antes de reinyectarlo.


Reintentar un mensaje venenoso de manera infinita bloquea por completo la partición de Kafka y detiene el procesamiento de cualquier otro mensaje válido posterior, provocando una caída en cascada del servicio. A diferencia de enviarlo a una DLT (donde queda diagnosticable y aislado) o perderlo de forma silenciosa, el bloqueo infinito consume recursos de CPU de forma inútil y paraliza la arquitectura orientada a eventos.

## Taller de Observabilidad Real en RICA

1. **Si `publicaciones-service` no pudiera marcar las publicaciones por un error permanente, ¿en qué estado quedaría el investigador y quién se daría cuenta?**
   El investigador se quedaría atrapado en el estado `EN_ELIMINACION`. De esto se daría cuenta el equipo de operaciones o monitoreo mediante una alerta basada en métricas o tableros que detecten registros con ese estado durante demasiado tiempo (Saga atascada).

2. **Diseña una compensación: ¿qué evento publicarías, quién lo consumiría y a qué estado volvería el investigador? ¿Sería idempotente?**
   Se publicaría un evento de compensación como `investigadores.cancelar-eliminacion`. Este evento sería consumido por `investigadores-service` para revertir el estado del investigador de `EN_ELIMINACION` de regreso a `ACTIVO`[cite: 6]. Sí, debe ser completamente idempotente para evitar inconsistencias si el mensaje se reintenta múltiples veces.

3. **¿Qué métrica y alerta definirías para detectar Sagas atascadas (por ejemplo, investigadores en `EN_ELIMINACION` hace más de 10 minutos)?**
   Definiría una métrica personalizada en MongoDB o Actuator que cuente el número de documentos con `estado == 'EN_ELIMINACION'` y una alerta de Prometheus/Grafana que dispare una notificación crítica si esa cantidad es mayor que cero por más de 10 minutos.

4. **¿Esta Saga conviene coreografiada u orquestada si se le agregaran cuatro pasos más?**
   cambiar a una Saga orquestada. Con tantos pasos en una coreografía, el acoplamiento y la dificultad para rastrear el flujo de negocio mediante eventos descentralizados se vuelven inmanejables, por lo que un orquestador central aporta claridad y control de errores.

## Taller de Contenedores y CI real 

* **(a) ¿Por qué se copia primero `pom.xml` y luego `src`?**
  Porque Docker construye las imágenes por capas y guarda en caché cada paso que no cambia. Como las dependencias del `pom.xml` cambian con mucha menos frecuencia que el código fuente en `src`, copiarlo y descargar las dependencias primero permite reutilizar esa capa pesada en futuras compilaciones, haciendo que reconstruir la imagen tras modificar el código tome apenas unos segundos.

* **(b) ¿Qué contiene la imagen final y qué no?**
  La imagen final contiene únicamente el entorno de ejecución ligero (JRE 25), el archivo compilado `app.jar` y el usuario sin privilegios `rica`. No contiene Maven, el JDK completo, los archivos fuente (`.java`), ni las dependencias temporales descargadas en la primera etapa, lo que reduce drásticamente su peso y superficie de ataque.

* **(c) ¿Por qué `USER rica`?**
  Por seguridad (principio de mínimo privilegio). Ejecutar la aplicación con un usuario del sistema sin privilegios (`rica`) en lugar de `root` evita que, ante una eventual vulnerabilidad en la aplicación, un atacante obtenga control administrativo total sobre el contenedor o el sistema anfitrión.
---

Al modificar únicamente un comentario en `application.yml` y volver a ejecutar `docker build`:
* **Se reutilizaron de la caché (`CACHED`):** La descarga de la imagen base de Maven, `WORKDIR /app`, `COPY pom.xml .` y la descarga de dependencias (`RUN mvn dependency:go-offline`).
* **Se ejecutaron de nuevo:** `COPY src ./src`, la compilación (`RUN mvn package`) y la copia del nuevo `.jar` hacia la imagen final de ejecución (`COPY --from=build`).

---

Configurar el servicio mediante variables de entorno (`-e SERVER_PORT=9000`) permite construir **un único artefacto inmutable** que pasa exactamente igual por desarrollo, pruebas y producción. Tener imágenes distintas por entorno rompe la garantía de que lo que se probó es exactamente lo que llega a producción, multiplica el almacenamiento en los registros y obliga a recompilar código cada vez que cambia una dirección de red o un puerto.
