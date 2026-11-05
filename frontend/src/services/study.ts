import { getJson, patchJson, postJson, putJson, unwrap } from "./http";

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

export type CycleKind = "TRABAJO" | "DESCANSO_CORTO" | "DESCANSO_LARGO";
export type CycleState = "NO_INICIADO" | "EN_CURSO" | "PAUSADO" | "COMPLETADO" | "DETENIDO";
export type CycleAction = "iniciar" | "pausar" | "reanudar" | "siguiente";

export type StudyCycle = {
  number: number;
  kind: CycleKind;
  plannedSeconds: number;
  elapsedSeconds: number;
  state: CycleState;
};

export type StudySession = {
  id: string;
  subjectId: string;
  topicId: string;
  subject: string;
  topic: string;
  sound: string;
  technique: string;
  startedAt: number;
  plannedCycles: number;
  completedCycles: number;
  cycle: StudyCycle;
  finished: SessionRecord | null;
  interruptions: { reason: string; createdAt: number }[];
};

export type SessionRecord = {
  elapsedMinutes: number;
  completedCycles: number;
  completed: boolean;
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

export type StartSessionInput = {
  subjectId: string;
  topicId: string;
  subject: string;
  topic: string;
  sound?: string;
};

export interface StudyGateway {
  getSettings(): Promise<StudySettings>;
  saveSettings(userId: string, settings: StudySettings): Promise<StudySettings>;
  getSuggestion(subjectId: string, topicId: string): Promise<StudySuggestion>;
  startSession(input: StartSessionInput): Promise<StudySession>;
  changeCycle(session: StudySession, action: CycleAction, elapsedSeconds?: number): Promise<StudySession>;
  saveInterruption(sessionId: string, reason?: string): Promise<{ reason: string; createdAt: number }>;
  finishSession(session: StudySession, elapsedSeconds: number, saveProgress: boolean): Promise<StudySession>;
  summarize(session: StudySession, concentration: number | null): SessionSummary;
}

export const SOUNDS: Record<string, string> = {
  SILENCIO: "Silencio",
  RUIDO_BLANCO: "Ruido blanco",
  NATURALEZA: "Naturaleza",
  LOFI: "Lo-fi",
};

type ApiSettings = { duracionTrabajoMin: number; monitoreoActivo: boolean };
type ApiSuggestion = { personalizada: boolean; ambiente: string; tecnica: string; motivo: string };
type ApiCycle = {
  numero: number;
  tipo: CycleKind;
  duracionPlaneadaSeg: number;
  transcurridoSeg: number;
  estado: CycleState;
};
type ApiSession = {
  id: string;
  materiaId: string;
  temaId: string;
  inicio: string;
  ciclosPlaneados: number;
  ciclosCompletados: number;
  ambiente: string | null;
  tecnica: string | null;
  cicloActual: ApiCycle;
  registro: { duracionEfectivaMin: number; ciclosCompletados: number; completada: boolean } | null;
};

function soundCode(label?: string) {
  return Object.keys(SOUNDS).find((code) => SOUNDS[code] === label);
}

function toSettings(settings: ApiSettings): StudySettings {
  return { pomodoroMinutes: settings.duracionTrabajoMin, monitoringEnabled: settings.monitoreoActivo };
}

function toSession(api: ApiSession, previous: Pick<StudySession, "subject" | "topic" | "interruptions">): StudySession {
  return {
    id: api.id,
    subjectId: api.materiaId,
    topicId: api.temaId,
    subject: previous.subject,
    topic: previous.topic,
    sound: api.ambiente ? SOUNDS[api.ambiente] : "Silencio",
    technique: api.tecnica ?? "Pomodoro clásico",
    startedAt: Date.parse(api.inicio),
    plannedCycles: api.ciclosPlaneados,
    completedCycles: api.ciclosCompletados,
    cycle: {
      number: api.cicloActual.numero,
      kind: api.cicloActual.tipo,
      plannedSeconds: api.cicloActual.duracionPlaneadaSeg,
      elapsedSeconds: api.cicloActual.transcurridoSeg,
      state: api.cicloActual.estado,
    },
    finished: api.registro && {
      elapsedMinutes: api.registro.duracionEfectivaMin,
      completedCycles: api.registro.ciclosCompletados,
      completed: api.registro.completada,
    },
    interruptions: previous.interruptions,
  };
}

export const studyGateway: StudyGateway = {
  async getSettings() {
    return toSettings(unwrap(await getJson<ApiSettings>("/api/configuracion-pomodoro")));
  },

  async saveSettings(_userId, settings) {
    return toSettings(unwrap(await putJson<ApiSettings>("/api/configuracion-pomodoro", {
      duracionTrabajoMin: settings.pomodoroMinutes,
      monitoreoActivo: settings.monitoringEnabled,
    })));
  },

  async getSuggestion(subjectId, topicId) {
    const query = new URLSearchParams({ materiaId: subjectId, temaId: topicId });
    const suggestion = unwrap(await getJson<ApiSuggestion>(`/api/sesiones/sugerencia?${query}`));
    return {
      sound: SOUNDS[suggestion.ambiente],
      technique: suggestion.tecnica,
      personalized: suggestion.personalizada,
      reason: suggestion.motivo,
    };
  },

  async startSession(input) {
    const api = unwrap(await postJson<ApiSession>("/api/sesiones", {
      materiaId: input.subjectId,
      temaId: input.topicId,
      ambiente: soundCode(input.sound),
    }));
    return toSession(api, { subject: input.subject, topic: input.topic, interruptions: [] });
  },

  async changeCycle(session, action, elapsedSeconds = 0) {
    const api = unwrap(await patchJson<ApiSession>(`/api/sesiones/${session.id}/ciclo`, {
      accion: action,
      transcurridoSeg: Math.max(0, Math.round(elapsedSeconds)),
    }));
    return toSession(api, session);
  },

  // HU08 (registrar pausa) aún no tiene endpoint.
  async saveInterruption(_sessionId, reason) {
    return {
      reason: reason?.trim() || "Sin motivo especificado",
      createdAt: Date.now(),
    };
  },

  async finishSession(session, elapsedSeconds, saveProgress) {
    const api = unwrap(await postJson<ApiSession>(`/api/sesiones/${session.id}/finalizar`, {
      transcurridoSeg: Math.max(0, Math.round(elapsedSeconds)),
      confirmado: true,
      guardarProgreso: saveProgress,
    }));
    return toSession(api, session);
  },

  summarize(session, concentration) {
    const record = session.finished ?? { elapsedMinutes: 0, completedCycles: session.completedCycles, completed: false };
    return {
      sessionId: session.id,
      elapsedMinutes: record.elapsedMinutes,
      completedCycles: record.completedCycles,
      interruptions: session.interruptions.length,
      averageConcentration: concentration,
      completed: record.completed,
      modelUpdated: true,
      recommendation:
        concentration !== null && concentration < 60
          ? "Prueba bloques cinco minutos más cortos y deja el teléfono fuera de alcance."
          : "Mantén este horario: tu concentración fue estable durante la sesión.",
    };
  },
};
