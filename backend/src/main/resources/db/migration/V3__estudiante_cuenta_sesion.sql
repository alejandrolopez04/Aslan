CREATE TABLE estudiantes (
    id             UUID         PRIMARY KEY,
    nombre         VARCHAR(120) NOT NULL,
    fecha_registro TIMESTAMPTZ  NOT NULL
);

INSERT INTO estudiantes (id, nombre, fecha_registro)
SELECT id, nombre, creada_en FROM cuentas;

DROP TABLE tokens_sesion;

ALTER TABLE cuentas RENAME COLUMN id TO estudiante_id;
ALTER TABLE cuentas DROP COLUMN nombre;
ALTER TABLE cuentas DROP COLUMN creada_en;
ALTER TABLE cuentas
    ADD CONSTRAINT fk_cuentas_estudiante FOREIGN KEY (estudiante_id) REFERENCES estudiantes (id) ON DELETE CASCADE;

CREATE TABLE sesiones_autenticacion (
    id            UUID          PRIMARY KEY,
    estudiante_id UUID          NOT NULL REFERENCES estudiantes (id) ON DELETE CASCADE,
    token         VARCHAR(1024) NOT NULL,
    inicio        TIMESTAMPTZ   NOT NULL,
    expiracion    TIMESTAMPTZ   NOT NULL,
    activa        BOOLEAN       NOT NULL
);

CREATE INDEX idx_sesiones_autenticacion_estudiante ON sesiones_autenticacion (estudiante_id);
