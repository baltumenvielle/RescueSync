# Integración con Bonita — guía para quienes implementan `BonitaBpmAdapter`

El backend ya funciona de punta a punta contra `FakeBpmAdapter`. Para conectarlo con Bonita alcanza con agregar una implementación real de `BpmPort` en el paquete `ar.edu.unlp.dssd.rescuesync.integration.bonita`, activa con el perfil `bonita`. **No debería ser necesario tocar ninguna otra clase**; si hace falta, avisen: significa que la interfaz quedó mal diseñada.

## 1. Contrato: `bpm/BpmPort.java`

```java
void iniciarSesion(UsuarioBpm usuario, String password);   // en cada login de la app (no persistir la contraseña)
void cerrarSesion(UsuarioBpm usuario);                     // logout
String iniciarCaso(UsuarioBpm usuario, Long emergenciaId); // POST /API/bpm/case con variable emergenciaId -> caseId
List<TareaPendiente> tareasDe(UsuarioBpm usuario);         // humanTask ready del usuario
void setVariable(UsuarioBpm usuario, String caseId, String nombre, Object valor); // PUT caseVariable
void completarTarea(UsuarioBpm usuario, String taskId);    // PUT humanTask assigned_id + POST userTask/{id}/execution
```

Cambios respecto del plan original (§7.5): todos los métodos reciben `UsuarioBpm` (id, username, `bonitaUsername`, `bonitaUserId`, rol) para que el adaptador use **la sesión del usuario que actúa**, y se agregaron `iniciarSesion`/`cerrarSesion` para el registro de sesiones de §6.2. El auditor no abre sesión en Bonita.

- `TareaPendiente(taskId, nombreTarea, caseId, emergenciaId, fechaDisponible)`: `emergenciaId` puede quedar en `null`; la app lo resuelve por `caseId`.
- `setVariable` recibe `Integer` (`horasVentana`) o `String` (`decisionCCR`); el `type` que espera Bonita es el nombre de clase completo (`valor.getClass().getName()`), y `value` va como string.
- `bonitaUserId` es nulo en la base; el adaptador puede resolverlo con `GET /API/identity/user?f=userName=...` (y persistirlo con `Usuario#setBonitaUserId` si quieren).
- Errores: lanzar `BpmException` (→ HTTP 502 con el mensaje) o `BpmSesionExpiradaException` si la sesión venció y no se puede renovar (→ HTTP 401 con `codigo: BPM_SESION_EXPIRADA`; la SPA desloguea).

### Activación

```java
@Component
@Profile("bonita")
public class BonitaBpmAdapter implements BpmPort { ... }
```

`FakeBpmAdapter` y el controller de simulación (`/api/dev/bpm/**`) están anotados con `@Profile("!bonita")`, así que se desactivan solos. Las propiedades `bonita.base-url`, `bonita.process-name` y `bonita.process-version` ya están en `application-bonita.yml`.

## 2. Qué espera la app del modelo

### Nombres de tareas humanas

La app reconoce las tareas por nombre (ignorando mayúsculas, tildes y espacios). Se configuran en `application.yml`:

| Clave | Valor por defecto | Pantalla de la SPA | Lane / actor |
|---|---|---|---|
| `registro` | Registro de Emergencia | (se completa sola al instanciar) | Municipio |
| `revision` | Revisión de Información | Revisión | CCR |
| `desglose` | Desglose en lotes de necesidades | Lotes + ventana | CCR |
| `evaluar-cobertura` | Evaluar cobertura insuficiente | Decisión | CCR |

Si el modelo **elimina** la tarea "Registro de Emergencia" (el evento de inicio ya representa el registro web), poner `rescuesync.bpm.completar-registro-al-instanciar: false`. Si la conservan, el backend la completa justo después de crear el caso; si Bonita tarda en generar la tarea, el adaptador debería reintentar brevemente dentro de `tareasDe` o el caso queda esperando en esa tarea (hoy solo se registra un warning).

### Variables de proceso

| Variable | Tipo | Quién la escribe |
|---|---|---|
| `emergenciaId` | `java.lang.Long` | `iniciarCaso` |
| `horasVentana` | `java.lang.Integer` | app, antes de completar *Desglose* y al decidir `REABRIR` |
| `coberturaCompleta` | `java.lang.Boolean` | conector: `bodyAsObject.completa` |
| `decisionCCR` | `java.lang.String` | app, antes de completar *Evaluar cobertura insuficiente* |
| `validacionCompletada` | `java.lang.Boolean` | (etapa siguiente) |

Orden en que la app llama al motor al completar una tarea: primero verifica que la tarea exista para el caso, guarda en Postgres, escribe variables y por último `completarTarea`. Si algo falla, la transacción local hace rollback.

### Endpoints para los conectores REST

Header obligatorio: `X-Internal-Token: <INTERNAL_TOKEN>`. Respuestas en JSON plano.

| Tarea automática | Llamada | Respuesta |
|---|---|---|
| Publicación de convocatoria | `POST /internal/bpm/emergencias/{emergenciaId}/convocatoria/publicar` | `{emergenciaId, publicada, horasVentana, fechaApertura, fechaCierre}` |
| Evaluación de cobertura (al vencer el timer) | `GET /internal/bpm/emergencias/{emergenciaId}/cobertura` | `{emergenciaId, completa, lotesTotales, lotesCubiertos}` → mapear `completa` a `coberturaCompleta` |
| Listado consolidado de ofertas | `GET /internal/bpm/emergencias/{emergenciaId}/ofertas` | `{emergenciaId, cantidad, ofertas:[{ofertaId, ongId, ongNombre, ongCuit, version, items:[{loteId, tipo, cantidad}]}]}` |

Publicar y cobertura son idempotentes (se pueden reintentar). Cobertura además marca la convocatoria como cerrada. Desde Bonita en Docker la URL base es `http://backend:8080`; desde Bonita Studio local, `http://localhost:8080`.

### Timer

La duración debe salir de `horasVentana` (por ejemplo, expresión Groovy que devuelva `horasVentana * 3600000L` ms según el tipo de timer). La app calcula la misma fecha de cierre al publicar y **rechaza ofertas pasada esa fecha aunque el conector de cobertura llegue tarde**.

### Usuarios espejo

Cada usuario de la app (salvo `auditor`) necesita un usuario en la organización de Bonita con el **mismo username y contraseña** (`rescuesync` en el seed), mapeado al actor de su lane.

## 3. Cómo probar sin la SPA

```bash
TOKEN=$(curl -s localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"ccr","password":"rescuesync"}' | jq -r .token)
curl -s localhost:8080/api/tareas -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/internal/bpm/emergencias/1/cobertura -H 'X-Internal-Token: dev-internal-token'
```
