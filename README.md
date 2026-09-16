# RescueSync

Plataforma de coordinación de la respuesta ante desastres — Trabajo integrador DSSD 2026, Grupo 9.

Este repositorio contiene la **Etapa 2** (sub-alcance de la entrega): alta de emergencias por el municipio, revisión y desglose en lotes por el CCR, publicación de la convocatoria, carga y edición versionada de ofertas por las ONGs y el camino alternativo ante cobertura insuficiente. La integración real con Bonita queda detrás de la interfaz `BpmPort` (ver [docs/integracion-bonita.md](docs/integracion-bonita.md)).

```
backend/    Spring Boot 4.1 · Java 25 · PostgreSQL · Flyway · JWT
frontend/   React 19 · TypeScript · Vite · Tailwind CSS 4 · TanStack Query
docs/       decisiones de diseño y guía de integración con Bonita
compose.yaml
```

## Arranque rápido (todo en contenedores)

```bash
docker compose up -d --build          # postgres + backend + frontend
# con Podman: podman compose up -d --build
```

- App web: http://localhost:5173
- API / Swagger: http://localhost:8080/swagger-ui.html
- Bonita (opcional): `docker compose --profile bonita up -d` → http://localhost:8081/bonita

Sin el perfil `bonita`, el backend usa **FakeBpmAdapter**, un motor de procesos simulado en memoria. En el detalle de la emergencia, el CCR ve un panel "Simulador del motor de procesos" con los botones que reemplazan a la publicación automática y al vencimiento del timer.

## Desarrollo local

Requisitos: JDK 25+, Node 24+, Docker o Podman.

```bash
docker compose up -d postgres          # PostgreSQL en localhost:5433

cd backend && ./mvnw spring-boot:run   # http://localhost:8080
cd frontend && npm install && npm run dev   # http://localhost:5173 (proxy /api -> 8080)
```

### Usuarios de prueba

Contraseña de todos: `rescuesync`

| Usuario | Perfil | Organización |
|---|---|---|
| `municipio.laplata` | Operador municipal | Municipalidad de La Plata |
| `municipio.berisso` | Operador municipal | Municipalidad de Berisso |
| `ccr` | Centro Coordinador Regional | CCR Capital |
| `ong.cruzroja`, `ong.caritas`, `ong.bomberos` | Representante de ONG | una ONG cada uno |
| `auditor` | Auditor / Directivo | Dirección Provincial de Auditoría |

### Recorrido de demo

1. `municipio.laplata` → **Nueva emergencia**.
2. `ccr` → **Bandeja de tareas** → *Revisar información* → *Definir lotes y publicar* (agregar lotes, fijar horas de ventana, confirmar).
3. `ccr` → detalle de la emergencia → **Publicar convocatoria** (simulador).
4. `ong.cruzroja` / `ong.caritas` → **Convocatorias abiertas** → cargar oferta parcial, editarla (nueva versión), enviarla. Ver **Historial de versiones**.
5. `ccr` → detalle → **Simular fin de ventana**. Si la cobertura es insuficiente aparece la tarea *Decidir ante cobertura insuficiente* (reabrir / reformular / continuar parcial).
6. `auditor` → emergencias en solo lectura y **Eventos de auditoría**.

## Tests

```bash
cd backend && ./mvnw test
```

Incluye tests unitarios de versionado de ofertas, cálculo de cobertura y ventana temporal, un test de arquitectura (nada fuera de `integration.bonita` depende de Bonita) y un test de integración del circuito completo con Testcontainers (requiere Docker o Podman).

Con Podman rootless:

```bash
systemctl --user start podman.socket
export DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock TESTCONTAINERS_RYUK_DISABLED=true
./mvnw test
```

Frontend: `npm run build` (typecheck + build) y `npm run lint`.

## Variables de entorno del backend

| Variable | Default | Uso |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5433/rescuesync`, `rescuesync`, `rescuesync` | Base de datos |
| `JWT_SECRET` | valor de desarrollo | Firma HMAC de los JWT (≥ 32 bytes) |
| `JWT_EXPIRACION` | `PT8H` | Duración del token |
| `INTERNAL_TOKEN` | `dev-internal-token` | Header `X-Internal-Token` de los conectores de Bonita |
| `SPRING_PROFILES_ACTIVE` | vacío | `bonita` desactiva el adaptador simulado |
