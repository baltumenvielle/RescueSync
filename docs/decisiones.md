# Registro de decisiones de diseño — Etapa 2

Decisiones no previstas en `PLAN_ETAPA_2_RescueSync.md` o que se apartan de él.

## 2026-09-16 — Alcance: sub-alcance de la Entrega 2

Se implementa lo que pide el enunciado para la Entrega 2 (alta de emergencia, lotes y publicación, carga básica de ofertas, integración inicial con el BPM), más lo imprescindible para que el circuito sea coherente: RBAC de 4 perfiles, versionado de ofertas, cobertura, camino alternativo del timer y auditoría.
Quedan para etapas siguientes: consorcios, inventario de ONG, validación externa y stub del Sistema Nacional, adjudicación, compromiso/liberación, actividades y trazabilidad completa del auditor. El esquema Flyway solo crea las tablas en uso; las demás llegan en migraciones nuevas.

## 2026-09-16 — Spring Boot 4.1 y Java 25 en lugar de Boot 3.3 y Java 21

start.spring.io ya no ofrece la línea 3.x y 3.3 no tiene soporte OSS. Java 25 es la LTS vigente. Impacto: Jackson 3 (`tools.jackson`), starters modularizados (`spring-boot-starter-webmvc`, `-flyway`) y springdoc 3.x. El frontend usa React 19, React Router 8 y Tailwind 4 por el mismo motivo.

## 2026-09-16 — `BpmPort` recibe el usuario en todas las operaciones

El plan pasaba solo `bonitaUserId` a `completarTarea` y nada a `setVariable`/`iniciarCaso`. Como cada usuario opera con su propia sesión de Bonita (§6.2), el adaptador necesita saber quién actúa. Se agregó el record `UsuarioBpm` (no la entidad JPA) y los métodos `iniciarSesion`/`cerrarSesion`. Detalle en `docs/integracion-bonita.md`.

## 2026-09-16 — Las acciones de dominio completan la tarea correspondiente

En lugar de un `POST /api/tareas/{taskId}/ejecutar` genérico (el proceso no tiene contratos), cada endpoint de dominio (`PUT /revision`, `POST /convocatoria`, `POST /decision-cobertura`) busca la tarea del caso por nombre, valida que el motor la tenga disponible, persiste, escribe las variables y la completa. Así el BPM decide qué paso está habilitado y la app no puede ejecutar una tarea sin sus datos.

## 2026-09-16 — Tarea "Registro de Emergencia"

La emergencia se registra en el formulario web antes de que exista el caso, así que la tarea humana del modelo queda redundante. Mientras el modelo la conserve, el backend la completa inmediatamente después de instanciar (`completar-registro-al-instanciar`). Recomendación para el modelo: eliminarla y dejar que el evento de inicio represente el registro.

## 2026-09-16 — Columnas agregadas a `emergencia`

`horas_ventana` (la app necesita la duración para calcular `fecha_cierre_convocatoria` al publicar), `observaciones_revision`, `revisada_por`, `fecha_revision` (resultado de la tarea de revisión) y `decision_ccr` (última decisión ante cobertura insuficiente, para mostrar y auditar).

## 2026-09-16 — Una oferta por ONG por emergencia; cada versión guarda la foto completa

Restricción única `(emergencia_id, ong_lider_id)`: las modificaciones son versiones de la misma oferta, no ofertas nuevas. Cada `oferta_version` guarda todos sus ítems (no solo los modificados): simplifica el cálculo de cobertura sobre la versión vigente y permite comparar versiones directamente. Editar una oferta ya enviada genera una versión nueva y la mantiene enviada.

## 2026-09-16 — Solo las ofertas ENVIADAS cuentan para la cobertura

Los borradores permiten a la ONG trabajar la oferta sin comprometerla. La cobertura (y el listado consolidado para el BPM) considera únicamente ofertas `ENVIADA` en su versión vigente.

## 2026-09-16 — Bloqueo pesimista sobre la emergencia

Toda operación sobre ofertas y el cierre de la convocatoria toman `SELECT ... FOR UPDATE` sobre la fila de la emergencia. Evita que una edición concurrente con el vencimiento del timer quede a medio camino entre abierta y cerrada.

## 2026-09-16 — `GET /internal/.../cobertura` cierra la convocatoria

El plan define el conector de cobertura como GET. Se mantuvo para no complicar la configuración del conector, pero además marca la convocatoria como `CONVOCATORIA_CERRADA` (de forma idempotente). La regla temporal no depende de esto: las ofertas se rechazan por `fecha_cierre_convocatoria` aunque el conector no haya corrido.

## 2026-09-16 — Endpoint consolidado de ofertas para el BPM

Además de los endpoints del plan se agregó `GET /internal/bpm/emergencias/{id}/ofertas`, que es lo que pide literalmente la directiva 3 del enunciado. Bonita no debe guardar ese listado en variables de caso (solo tipos simples); puede reenviarlo a la API del Sistema Nacional en el mismo conector o delegarlo al backend.

## 2026-09-16 — Simulador del motor en modo sin Bonita

`FakeBpmAdapter` emula el recorrido de tareas humanas y `FakeBpmController` (`/api/dev/bpm/**`, solo CCR, perfil `!bonita`) dispara la publicación y el vencimiento del timer usando los mismos servicios que los endpoints internos. Permite hacer la demo y los tests sin Bonita. El estado del fake vive en memoria: al reiniciar el backend, los casos existentes pierden sus tareas.

## 2026-09-16 — Supuesto: misma contraseña en la app y en Bonita

Tal como indica el plan (§6.2): cada usuario tiene su espejo en Bonita con la misma contraseña; la app no la persiste. En producción se resolvería con un proveedor de identidad común.

## 2026-09-16 — Errores de acceso

Recurso de otra organización → 403; inexistente → 404; regla de negocio o de estado violada → 409; validación de formulario → 400 con el mapa `errores` por campo. Todas las respuestas usan RFC 7807 sin stacktrace.
