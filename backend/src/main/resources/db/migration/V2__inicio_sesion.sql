ALTER TABLE cuentas
    ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0,
    ADD COLUMN bloqueada_hasta   TIMESTAMPTZ;

CREATE TABLE tokens_sesion (
    token     VARCHAR(64) PRIMARY KEY,
    cuenta_id UUID        NOT NULL REFERENCES cuentas (id) ON DELETE CASCADE,
    creado_en TIMESTAMPTZ NOT NULL,
    expira_en TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tokens_sesion_cuenta ON tokens_sesion (cuenta_id);
