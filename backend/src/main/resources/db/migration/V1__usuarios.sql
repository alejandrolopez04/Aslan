CREATE TABLE cuentas (
    id              UUID         PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    correo          VARCHAR(254) NOT NULL,
    contrasena_hash VARCHAR(100) NOT NULL,
    creada_en       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_cuentas_correo UNIQUE (correo)
);
