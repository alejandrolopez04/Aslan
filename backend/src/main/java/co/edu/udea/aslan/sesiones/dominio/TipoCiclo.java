package co.edu.udea.aslan.sesiones.dominio;

public enum TipoCiclo {

    TRABAJO {
        @Override
        public CicloPomodoro reconstruir(int numero, int duracionPlaneadaSeg, int transcurridoSeg, EstadoCiclo estado) {
            return new CicloTrabajo(numero, duracionPlaneadaSeg, transcurridoSeg, estado);
        }
    },
    DESCANSO_CORTO {
        @Override
        public CicloPomodoro reconstruir(int numero, int duracionPlaneadaSeg, int transcurridoSeg, EstadoCiclo estado) {
            return new CicloDescanso(numero, duracionPlaneadaSeg, transcurridoSeg, estado, false);
        }
    },
    DESCANSO_LARGO {
        @Override
        public CicloPomodoro reconstruir(int numero, int duracionPlaneadaSeg, int transcurridoSeg, EstadoCiclo estado) {
            return new CicloDescanso(numero, duracionPlaneadaSeg, transcurridoSeg, estado, true);
        }
    };

    public abstract CicloPomodoro reconstruir(int numero, int duracionPlaneadaSeg, int transcurridoSeg,
                                              EstadoCiclo estado);
}
