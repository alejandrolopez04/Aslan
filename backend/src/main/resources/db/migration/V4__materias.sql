CREATE TABLE materias (
    id            UUID PRIMARY KEY,
    estudiante_id UUID         NOT NULL,
    nombre        VARCHAR(80)  NOT NULL,
    color         VARCHAR(20)  NOT NULL,
    icono         VARCHAR(20)  NOT NULL,
    archivada     BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_materias_estudiante ON materias (estudiante_id);

CREATE TABLE temas (
    id           UUID PRIMARY KEY,
    materia_id   UUID         NOT NULL REFERENCES materias (id) ON DELETE CASCADE,
    posicion     INTEGER      NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    estado       VARCHAR(20)  NOT NULL,
    prioridad    VARCHAR(10)  NOT NULL,
    fecha_limite DATE,
    notas        TEXT         NOT NULL DEFAULT ''
);

CREATE INDEX idx_temas_materia ON temas (materia_id);
