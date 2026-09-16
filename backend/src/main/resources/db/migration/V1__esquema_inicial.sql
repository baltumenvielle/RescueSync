-- RescueSync - Esquema inicial (Etapa 2)
-- Cubre el sub-alcance de la entrega: usuarios/organizaciones, emergencias, lotes,
-- ofertas con versionado y auditoría. Consorcios, inventario, validaciones externas,
-- adjudicaciones y actividades se agregan en migraciones posteriores.

CREATE TABLE organizacion (
    id          BIGSERIAL PRIMARY KEY,
    tipo        VARCHAR(20)  NOT NULL CHECK (tipo IN ('MUNICIPIO', 'CCR', 'ONG', 'AUDITORIA')),
    nombre      VARCHAR(200) NOT NULL,
    cuit        VARCHAR(13)  UNIQUE,
    contacto    VARCHAR(200)
);

CREATE TABLE usuario (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(60)  NOT NULL UNIQUE,
    password_hash    VARCHAR(100) NOT NULL,
    email            VARCHAR(200) NOT NULL,
    nombre           VARCHAR(200) NOT NULL,
    rol              VARCHAR(30)  NOT NULL CHECK (rol IN ('OPERADOR_MUNICIPAL', 'CCR', 'REPRESENTANTE_ONG', 'AUDITOR')),
    organizacion_id  BIGINT       NOT NULL REFERENCES organizacion (id),
    bonita_username  VARCHAR(60),
    bonita_user_id   BIGINT,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE emergencia (
    id                          BIGSERIAL PRIMARY KEY,
    municipio_id                BIGINT       NOT NULL REFERENCES organizacion (id),
    tipo_desastre               VARCHAR(30)  NOT NULL,
    gravedad                    VARCHAR(10)  NOT NULL CHECK (gravedad IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')),
    zona_afectada               VARCHAR(300) NOT NULL,
    descripcion                 TEXT         NOT NULL,
    estado                      VARCHAR(30)  NOT NULL CHECK (estado IN ('REGISTRADA', 'EN_REVISION', 'LOTES_DEFINIDOS',
                                    'CONVOCATORIA_ABIERTA', 'CONVOCATORIA_CERRADA', 'EN_VALIDACION',
                                    'EN_ADJUDICACION', 'EN_EJECUCION', 'CERRADA')),
    case_id_bonita              VARCHAR(40)  UNIQUE,
    observaciones_revision      TEXT,
    revisada_por                BIGINT       REFERENCES usuario (id),
    fecha_revision              TIMESTAMPTZ,
    horas_ventana               INTEGER      CHECK (horas_ventana > 0),
    fecha_apertura_convocatoria TIMESTAMPTZ,
    fecha_cierre_convocatoria   TIMESTAMPTZ,
    decision_ccr                VARCHAR(20)  CHECK (decision_ccr IN ('REABRIR', 'REFORMULAR', 'CONTINUAR_PARCIAL')),
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by                  BIGINT       NOT NULL REFERENCES usuario (id)
);

CREATE INDEX idx_emergencia_municipio ON emergencia (municipio_id);
CREATE INDEX idx_emergencia_estado ON emergencia (estado);

CREATE TABLE lote (
    id                  BIGSERIAL PRIMARY KEY,
    emergencia_id       BIGINT        NOT NULL REFERENCES emergencia (id) ON DELETE CASCADE,
    tipo                VARCHAR(10)   NOT NULL CHECK (tipo IN ('PERSONAL', 'RECURSO')),
    descripcion         VARCHAR(300)  NOT NULL,
    unidad              VARCHAR(50)   NOT NULL,
    cantidad_requerida  INTEGER       NOT NULL CHECK (cantidad_requerida > 0),
    prioridad           VARCHAR(10)   NOT NULL CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA'))
);

CREATE INDEX idx_lote_emergencia ON lote (emergencia_id);

CREATE TABLE oferta (
    id              BIGSERIAL PRIMARY KEY,
    emergencia_id   BIGINT      NOT NULL REFERENCES emergencia (id),
    ong_lider_id    BIGINT      NOT NULL REFERENCES organizacion (id),
    estado          VARCHAR(20) NOT NULL CHECK (estado IN ('BORRADOR', 'ENVIADA', 'VALIDADA', 'ADJUDICADA', 'NO_ADJUDICADA')),
    version_actual  INTEGER     NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Una ONG tiene una única oferta por emergencia; los cambios generan versiones.
    CONSTRAINT uq_oferta_emergencia_ong UNIQUE (emergencia_id, ong_lider_id)
);

CREATE TABLE oferta_version (
    id          BIGSERIAL PRIMARY KEY,
    oferta_id   BIGINT      NOT NULL REFERENCES oferta (id),
    numero      INTEGER     NOT NULL,
    autor_id    BIGINT      NOT NULL REFERENCES usuario (id),
    comentario  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_oferta_version UNIQUE (oferta_id, numero)
);

CREATE TABLE oferta_item (
    id                  BIGSERIAL PRIMARY KEY,
    oferta_version_id   BIGINT      NOT NULL REFERENCES oferta_version (id),
    lote_id             BIGINT      NOT NULL REFERENCES lote (id),
    cantidad_ofrecida   INTEGER     NOT NULL CHECK (cantidad_ofrecida > 0),
    rol                 VARCHAR(10) CHECK (rol IN ('PRINCIPAL', 'APOYO')),
    CONSTRAINT uq_oferta_item_lote UNIQUE (oferta_version_id, lote_id)
);

CREATE TABLE evento_auditoria (
    id          BIGSERIAL PRIMARY KEY,
    entidad     VARCHAR(50)  NOT NULL,
    entidad_id  BIGINT,
    accion      VARCHAR(60)  NOT NULL,
    usuario_id  BIGINT       REFERENCES usuario (id),
    payload     JSONB,
    timestamp   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_evento_entidad ON evento_auditoria (entidad, entidad_id);
