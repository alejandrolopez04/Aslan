import type { Subject } from "./catalog";
import type { WeeklyPlan } from "./planner";
import type { SessionSummary } from "./study";

export type ConcentrationPoint = { label: string; value: number };
export type SubjectProgressMetric = {
  subjectId: string;
  name: string;
  progress: number;
  overdue: number;
  critical: boolean;
};

export type ActivitySummary = {
  id: string;
  type: "daily" | "weekly";
  dateLabel: string;
  periodStart: string;
  periodEnd: string;
  minutes: number;
  sessions: number;
  concentration: number;
  completedTopics: number;
  insight: string;
};

export type ActivityDay = {
  date: string;
  hadActivity: boolean;
  minutes: number;
};

export type DashboardData = {
  hasActivity: boolean;
  concentrationWeekly: ConcentrationPoint[];
  concentrationMonthly: ConcentrationPoint[];
  subjects: SubjectProgressMetric[];
  plannedSessions: number;
  completedSessions: number;
  streakCurrent: number;
  streakMax: number;
  totalMinutesThisWeek: number;
  activityDays: ActivityDay[];
  summaries: ActivitySummary[];
};

export interface AnalyticsGateway {
  getDashboard(userId: string, subjects: Subject[], plan: WeeklyPlan | null): Promise<DashboardData>;
  recordSession(userId: string, summary: SessionSummary): Promise<void>;
  exportDashboard(data: DashboardData, format: "pdf" | "csv" | "json"): Promise<Blob>;
  exportSummaries(summaries: ActivitySummary[], format: "pdf" | "csv" | "json"): Promise<Blob>;
}

const demoUsers = new Set(["usr_alejo"]);
const recordedSessions = new Map<string, SessionSummary[]>();

function dayKey(offset: number) {
  const date = new Date();
  date.setDate(date.getDate() + offset);
  return date.toISOString().slice(0, 10);
}

function escapePdfText(value: string) {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[()\\]/g, "\\$&");
}

function pdfFromLines(lines: string[]) {
  const stream = ["BT", "/F1 11 Tf", "45 795 Td",
    ...lines.slice(0, 38).flatMap((line, index) => [index ? "0 -18 Td" : "", `(${escapePdfText(line).slice(0, 100)}) Tj`]).filter(Boolean), "ET"].join("\n");
  const objects = [
    "<< /Type /Catalog /Pages 2 0 R >>",
    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
    `<< /Length ${stream.length} >>\nstream\n${stream}\nendstream`,
    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
  ];
  let pdf = "%PDF-1.4\n";
  const offsets = [0];
  objects.forEach((object, index) => {
    offsets.push(pdf.length);
    pdf += `${index + 1} 0 obj\n${object}\nendobj\n`;
  });
  const xref = pdf.length;
  pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
  pdf += offsets.slice(1).map((offset) => `${String(offset).padStart(10, "0")} 00000 n \n`).join("");
  return `${pdf}trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF`;
}

function subjectMetrics(subjects: Subject[] | undefined): SubjectProgressMetric[] {
  const safeSubjects = Array.isArray(subjects) ? subjects : [];
  return safeSubjects.filter((subject) => subject && !subject.archived).map((subject) => {
    const topics = Array.isArray(subject.topics) ? subject.topics : [];
    const completed = topics.filter((topic) => topic.status === "Completado").length;
    const progress = topics.length ? Math.round((completed / topics.length) * 100) : 0;
    const overdue = topics.filter((topic) =>
      topic.status !== "Completado" && topic.dueDate && topic.dueDate < new Date().toISOString().slice(0, 10),
    ).length;
    return {
      subjectId: subject.id,
      name: subject.name,
      progress,
      overdue,
      critical: topics.length >= 2 && progress < 35,
    };
  });
}

function demoSummaries(): ActivitySummary[] {
  return [
    {
      id: "daily_today",
      type: "daily",
      dateLabel: "Hoy",
      periodStart: dayKey(0),
      periodEnd: dayKey(0),
      minutes: 75,
      sessions: 3,
      concentration: 82,
      completedTopics: 1,
      insight: "Tu mejor bloque fue durante la tarde, con concentración estable.",
    },
    {
      id: "daily_yesterday",
      type: "daily",
      dateLabel: "Ayer",
      periodStart: dayKey(-1),
      periodEnd: dayKey(-1),
      minutes: 50,
      sessions: 2,
      concentration: 74,
      completedTopics: 0,
      insight: "Las pausas frecuentes redujeron ligeramente tu concentración.",
    },
    {
      id: "daily_3",
      type: "daily",
      dateLabel: "Hace 3 días",
      periodStart: dayKey(-3),
      periodEnd: dayKey(-3),
      minutes: 90,
      sessions: 3,
      concentration: 79,
      completedTopics: 2,
      insight: "Completaste todos los bloques planificados para el día.",
    },
    {
      id: "weekly_current",
      type: "weekly",
      dateLabel: "Esta semana",
      periodStart: dayKey(-6),
      periodEnd: dayKey(0),
      minutes: 315,
      sessions: 11,
      concentration: 78,
      completedTopics: 4,
      insight: "Aumentaste 8% tu tiempo efectivo respecto a la semana anterior.",
    },
    {
      id: "weekly_previous",
      type: "weekly",
      dateLabel: "Semana anterior",
      periodStart: dayKey(-13),
      periodEnd: dayKey(-7),
      minutes: 290,
      sessions: 10,
      concentration: 73,
      completedTopics: 3,
      insight: "Los bloques de 25 minutos produjeron los resultados más constantes.",
    },
  ];
}

function toCsv(rows: string[][]) {
  return rows.map((row) => row.map((cell) => `"${cell.replaceAll('"', '""')}"`).join(",")).join("\n");
}

export const analyticsGateway: AnalyticsGateway = {
  async getDashboard(userId, subjects, plan) {
    const userSessions = recordedSessions.get(userId) ?? [];
    const hasActivity = demoUsers.has(userId) || userSessions.length > 0;
    const baseWeekly = [68, 72, 70, 78, 75, 81, 84];
    const baseMonthly = [64, 69, 72, 71, 76, 78, 82, 80];
    const latest = userSessions.at(-1)?.averageConcentration;
    if (latest !== null && latest !== undefined) baseWeekly[baseWeekly.length - 1] = latest;
    const summaries = hasActivity ? demoSummaries() : [];
    if (userSessions.length) {
      const minutes = userSessions.reduce((sum, session) => sum + session.elapsedMinutes, 0);
      summaries.unshift({
        id: `daily_recorded_${userSessions.length}`,
        type: "daily",
        dateLabel: "Actividad reciente",
        periodStart: dayKey(0),
        periodEnd: dayKey(0),
        minutes,
        sessions: userSessions.length,
        concentration: Math.round(userSessions.reduce((sum, session) => sum + (session.averageConcentration ?? 0), 0) / userSessions.length),
        completedTopics: 0,
        insight: "Esta actividad acaba de incorporarse a tu modelo de rendimiento.",
      });
    }
    const planSessions = Array.isArray(plan?.sessions) ? plan.sessions : null;
    const plannedSessions = planSessions?.length ?? (hasActivity ? 12 : 0);
    const completedSessions = planSessions?.filter((session) => session.status === "Realizada").length ?? (hasActivity ? 9 : 0);
    return {
      hasActivity,
      concentrationWeekly: hasActivity ? baseWeekly.map((value, index) => ({ label: ["L", "M", "X", "J", "V", "S", "D"][index], value })) : [],
      concentrationMonthly: hasActivity ? baseMonthly.map((value, index) => ({ label: `S${index + 1}`, value })) : [],
      subjects: subjectMetrics(subjects),
      plannedSessions,
      completedSessions,
      streakCurrent: hasActivity ? 6 + (userSessions.length ? 1 : 0) : 0,
      streakMax: hasActivity ? Math.max(9, 6 + userSessions.length) : 0,
      totalMinutesThisWeek: hasActivity ? 315 + userSessions.reduce((sum, session) => sum + session.elapsedMinutes, 0) : 0,
      activityDays: hasActivity ? [
        { date: dayKey(-6), hadActivity: true, minutes: 40 },
        { date: dayKey(-5), hadActivity: true, minutes: 50 },
        { date: dayKey(-4), hadActivity: false, minutes: 0 },
        { date: dayKey(-3), hadActivity: true, minutes: 90 },
        { date: dayKey(-2), hadActivity: true, minutes: 60 },
        { date: dayKey(-1), hadActivity: true, minutes: 50 },
        { date: dayKey(0), hadActivity: true, minutes: 75 },
      ] : [{ date: dayKey(0), hadActivity: false, minutes: 0 }],
      summaries,
    };
  },
  async recordSession(userId, summary) {
    recordedSessions.set(userId, [...(recordedSessions.get(userId) ?? []), summary]);
  },
  async exportDashboard(data, format) {
    if (format === "json") return new Blob([JSON.stringify(data, null, 2)], { type: "application/json" });
    const rows = [
      ["Métrica", "Valor"],
      ["Racha actual", String(data.streakCurrent)],
      ["Racha máxima", String(data.streakMax)],
      ["Minutos esta semana", String(data.totalMinutesThisWeek)],
      ["Sesiones planificadas", String(data.plannedSessions)],
      ["Sesiones realizadas", String(data.completedSessions)],
      ...data.subjects.map((subject) => [`Progreso ${subject.name}`, `${subject.progress}%`]),
    ];
    if (format === "csv") return new Blob([toCsv(rows)], { type: "text/csv;charset=utf-8" });
    return new Blob([pdfFromLines(["Aslan - Tablero de rendimiento", "", ...rows.map((row) => row.join(": "))])], { type: "application/pdf" });
  },
  async exportSummaries(summaries, format) {
    if (format === "json") return new Blob([JSON.stringify(summaries, null, 2)], { type: "application/json" });
    const rows = [
      ["Periodo", "Tipo", "Minutos", "Sesiones", "Concentración", "Temas completados", "Análisis"],
      ...summaries.map((summary) => [
        summary.dateLabel, summary.type, String(summary.minutes), String(summary.sessions),
        `${summary.concentration}%`, String(summary.completedTopics), summary.insight,
      ]),
    ];
    if (format === "csv") return new Blob([toCsv(rows)], { type: "text/csv;charset=utf-8" });
    return new Blob([pdfFromLines(["Aslan - Resumenes de actividad", "", ...rows.slice(1).map((row) => row.join(" | "))])], { type: "application/pdf" });
  },
};
