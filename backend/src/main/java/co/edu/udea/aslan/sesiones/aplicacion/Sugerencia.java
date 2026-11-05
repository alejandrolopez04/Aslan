package co.edu.udea.aslan.sesiones.aplicacion;

import co.edu.udea.aslan.sesiones.dominio.TipoSonido;

public record Sugerencia(boolean personalizada, TipoSonido ambiente, String tecnica, String motivo) {

    static final Sugerencia ESTANDAR = new Sugerencia(false, TipoSonido.SILENCIO, "Pomodoro clásico",
            "Aún no hay sesiones previas. Usaremos una configuración estándar.");
}
