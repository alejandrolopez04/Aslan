export type StudySettings = {
  pomodoroMinutes: number;
  monitoringEnabled: boolean;
};

export type StudySuggestion = {
  sound: string;
  technique: string;
  personalized: boolean;
  reason: string;
};

export type StudySession = {
  id: string;
  subject: string;
  topic: string;
  sound: string;
  technique: string;
  startedAt: number;
  plannedCycles: number;
  completedCycles: number;
  interruptions: { reason: string; createdAt: number }[];
};

export type SessionSummary = {
  sessionId: string;
  elapsedMinutes: number;
  completedCycles: number;
  interruptions: number;
  averageConcentration: number | null;
  completed: boolean;
  modelUpdated: boolean;
  recommendation: string;
};

export interface StudyGateway {
  getSuggestion(userId: string, subject: string, topic: string): Promise<StudySuggestion>;
  saveSettings(userId: string, settings: StudySettings): Promise<StudySettings>;
  startSession(input: Omit<StudySession, "id" | "startedAt" | "completedCycles" | "interruptions">): Promise<StudySession>;
  saveInterruption(sessionId: string, reason?: string): Promise<{ reason: string; createdAt: number }>;
  finishSession(session: StudySession, concentration: number | null, completed: boolean): Promise<SessionSummary>;
}

const usersWithHistory = new Set(["usr_sofia", "usr_ana", "usr_demo"]);

/**
 * Adaptador temporal. Conserva el mismo contrato que podrá implementar la API
 * de Spring Boot sin acoplar los componentes a datos simulados.
 */
export const studyGateway: StudyGateway = {
  async getSuggestion(userId, subject) {
    const personalized = usersWithHistory.has(userId);
    if (!personalized) {
      return {
        sound: "Silencio",
        technique: "Pomodoro clásico",
        personalized: false,
        reason: "Aún no hay sesiones previas. Usaremos una configuración estándar.",
      };
    }

    const scienceSubject = ["Matemáticas", "Física", "Programación"].includes(subject);
    return {
      sound: scienceSubject ? "Ruido marrón" : "Café tranquilo",
      technique: scienceSubject ? "Práctica intercalada" : "Recuerdo activo",
      personalized: true,
      reason: scienceSubject
        ? "Sueles mantener más concentración con sonido constante y bloques prácticos."
        : "Tus mejores resultados aparecen al combinar lectura y recuperación activa.",
    };
  },

  async saveSettings(_userId, settings) {
    return settings;
  },

  async startSession(input) {
    return {
      ...input,
      id: crypto.randomUUID(),
      startedAt: Date.now(),
      completedCycles: 0,
      interruptions: [],
    };
  },

  async saveInterruption(_sessionId, reason) {
    return {
      reason: reason?.trim() || "Sin motivo especificado",
      createdAt: Date.now(),
    };
  },

  async finishSession(session, concentration, completed) {
    const elapsedMinutes = Math.max(
      1,
      Math.round((Date.now() - session.startedAt) / 60_000),
    );
    return {
      sessionId: session.id,
      elapsedMinutes,
      completedCycles: session.completedCycles,
      interruptions: session.interruptions.length,
      averageConcentration: concentration,
      completed,
      modelUpdated: true,
      recommendation:
        concentration !== null && concentration < 60
          ? "Prueba bloques cinco minutos más cortos y deja el teléfono fuera de alcance."
          : "Mantén este horario: tu concentración fue estable durante la sesión.",
    };
  },
};
