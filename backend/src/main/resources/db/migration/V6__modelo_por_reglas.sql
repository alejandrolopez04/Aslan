CREATE TABLE perfiles_aprendizaje (
    estudiante_id       UUID PRIMARY KEY,
    sesiones_procesadas INTEGER NOT NULL
);

CREATE TABLE pesos_ambiente (
    estudiante_id UUID             NOT NULL REFERENCES perfiles_aprendizaje (estudiante_id) ON DELETE CASCADE,
    ambiente      VARCHAR(20)      NOT NULL,
    peso          DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (estudiante_id, ambiente)
);
