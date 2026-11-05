package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.ConfiguracionPomodoro;
import co.edu.udea.aslan.sesiones.dominio.SesionEstudio;

import java.time.Instant;

public enum AccionCiclo {

    INICIAR {
        @Override
        void aplicar(SesionEstudio sesion, int transcurridoSeg, ConfiguracionPomodoro cfg, Instant ahora) {
            sesion.iniciarCiclo();
        }
    },
    PAUSAR {
        @Override
        void aplicar(SesionEstudio sesion, int transcurridoSeg, ConfiguracionPomodoro cfg, Instant ahora) {
            sesion.pausar(transcurridoSeg);
        }
    },
    REANUDAR {
        @Override
        void aplicar(SesionEstudio sesion, int transcurridoSeg, ConfiguracionPomodoro cfg, Instant ahora) {
            sesion.reanudar();
        }
    },
    SIGUIENTE {
        @Override
        void aplicar(SesionEstudio sesion, int transcurridoSeg, ConfiguracionPomodoro cfg, Instant ahora) {
            sesion.avanzarCiclo(transcurridoSeg, cfg, ahora);
        }
    };

    abstract void aplicar(SesionEstudio sesion, int transcurridoSeg, ConfiguracionPomodoro cfg, Instant ahora);
}
