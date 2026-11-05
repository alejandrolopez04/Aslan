CREATE TABLE configuraciones_pomodoro (
    estudiante_id              UUID PRIMARY KEY,
    duracion_trabajo_min       INTEGER NOT NULL,
    descanso_corto_min         INTEGER NOT NULL,
    descanso_largo_min         INTEGER NOT NULL,
    ciclos_para_descanso_largo INTEGER NOT NULL,
    monitoreo_activo           BOOLEAN NOT NULL
);

CREATE TABLE sesiones_estudio (
    id               UUID PRIMARY KEY,
    estudiante_id    UUID        NOT NULL,
    materia_id       UUID        NOT NULL,
    tema_id          UUID        NOT NULL,
    inicio           TIMESTAMPTZ NOT NULL,
    fin              TIMESTAMPTZ,
    estado           VARCHAR(30) NOT NULL,
    ciclos_planeados INTEGER     NOT NULL,
    ambiente         VARCHAR(20),
    tecnica          VARCHAR(80)
);

CREATE INDEX idx_sesiones_estudio_estudiante ON sesiones_estudio (estudiante_id);

CREATE TABLE ciclos_pomodoro (
    sesion_id             UUID        NOT NULL REFERENCES sesiones_estudio (id) ON DELETE CASCADE,
    posicion              INTEGER     NOT NULL,
    tipo                  VARCHAR(20) NOT NULL,
    numero                INTEGER     NOT NULL,
    duracion_planeada_seg INTEGER     NOT NULL,
    transcurrido_seg      INTEGER     NOT NULL,
    estado                VARCHAR(20) NOT NULL,
    PRIMARY KEY (sesion_id, posicion)
);

CREATE TABLE registros_sesion (
    sesion_id             UUID PRIMARY KEY REFERENCES sesiones_estudio (id) ON DELETE CASCADE,
    estudiante_id         UUID        NOT NULL,
    materia_id            UUID        NOT NULL,
    tema_id               UUID        NOT NULL,
    ambiente              VARCHAR(20),
    fecha                 TIMESTAMPTZ NOT NULL,
    duracion_efectiva_min INTEGER     NOT NULL,
    ciclos_completados    INTEGER     NOT NULL,
    completada            BOOLEAN     NOT NULL
);

CREATE INDEX idx_registros_sesion_estudiante ON registros_sesion (estudiante_id);

CREATE TABLE rachas_estudio (
    estudiante_id      UUID PRIMARY KEY,
    racha_actual       INTEGER NOT NULL,
    racha_maxima       INTEGER NOT NULL,
    ultimo_dia_estudio DATE
);
