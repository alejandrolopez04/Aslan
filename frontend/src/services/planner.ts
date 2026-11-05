import type { Subject, Topic } from "./catalog";

export type PlanSessionStatus = "Propuesta" | "Programada" | "Realizada";
export type PlanSessionType = "Estudio" | "Repaso";

export type PlanSession = {
  id: string;
  subjectId: string;
  subjectName: string;
  topicId: string;
  topicName: string;
  date: string;
  time: string;
  duration: number;
  type: PlanSessionType;
  status: PlanSessionStatus;
};

export type WeeklyPlan = {
  id: string;
  weekStart: string;
  generatedAt: number;
  sessions: PlanSession[];
};

export type ReviewPlanInput = {
  id: string;
  subjectId: string;
  subjectName: string;
  topicId: string;
  topicName: string;
  dueDate: string;
  status: string;
};

export interface PlannerGateway {
  generate(subjects: Subject[], duration: number, reviews?: ReviewPlanInput[]): Promise<WeeklyPlan | null>;
  save(plan: WeeklyPlan): Promise<WeeklyPlan>;
  exportPlan(plan: WeeklyPlan, format: "pdf" | "csv"): Promise<Blob>;
}

function startOfWeek(date = new Date()) {
  const result = new Date(date);
  const day = result.getDay() || 7;
  result.setDate(result.getDate() - day + 1);
  result.setHours(12, 0, 0, 0);
  return result;
}

function dateKey(date: Date) {
  return date.toISOString().slice(0, 10);
}

function addDays(date: Date, days: number) {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function escapePdfText(value: string) {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[()\\]/g, "\\$&");
}

function pdfFromLines(lines: string[]) {
  const stream = [
    "BT", "/F1 11 Tf", "45 795 Td",
    ...lines.slice(0, 38).flatMap((line, index) => [index ? "0 -18 Td" : "", `(${escapePdfText(line).slice(0, 100)}) Tj`]).filter(Boolean),
    "ET",
  ].join("\n");
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

function topicWeight(topic: Topic) {
  let weight = topic.priority === "Alta" ? 4 : topic.priority === "Media" ? 2 : 1;
  if (topic.status === "En progreso") weight += 2;
  if (topic.dueDate) {
    const days = (new Date(topic.dueDate).getTime() - Date.now()) / 86_400_000;
    if (days <= 7) weight += 3;
  }
  return weight;
}

export const plannerGateway: PlannerGateway = {
  async generate(subjects, duration, reviews = []) {
    const available = subjects
      .filter((subject) => !subject.archived)
      .flatMap((subject) => subject.topics
        .filter((topic) => topic.status !== "Completado")
        .map((topic) => ({ subject, topic })))
      .sort((a, b) => topicWeight(b.topic) - topicWeight(a.topic));
    if (!available.length && !reviews.some((review) => review.status !== "Completado")) return null;

    const week = startOfWeek();
    const slots = [
      { day: 0, time: "18:00" }, { day: 1, time: "19:00" },
      { day: 2, time: "18:00" }, { day: 3, time: "19:00" },
      { day: 4, time: "17:00" }, { day: 5, time: "10:00" },
    ];
    const sessions: PlanSession[] = [];
    available.slice(0, 6).forEach(({ subject, topic }, index) => {
      const slot = slots[index % slots.length];
      sessions.push({
        id: crypto.randomUUID(),
        subjectId: subject.id,
        subjectName: subject.name,
        topicId: topic.id,
        topicName: topic.name,
        date: dateKey(addDays(week, slot.day)),
        time: slot.time,
        duration,
        type: "Estudio",
        status: "Propuesta",
      });
    });

    if (reviews.length) {
      const weekEnd = dateKey(addDays(week, 6));
      reviews
        .filter((review) => review.status !== "Completado" && review.dueDate >= dateKey(week) && review.dueDate <= weekEnd)
        .slice(0, 4)
        .forEach((review, index) => {
          sessions.push({
            id: crypto.randomUUID(),
            subjectId: review.subjectId,
            subjectName: review.subjectName,
            topicId: review.topicId,
            topicName: review.topicName,
            date: review.dueDate,
            time: index % 2 ? "18:30" : "19:00",
            duration: Math.min(20, duration),
            type: "Repaso",
            status: "Propuesta",
          });
        });
    } else {
      const reviewCandidates = available.filter(({ topic }) => topic.scheduledReviews > 0);
      reviewCandidates.slice(0, 2).forEach(({ subject, topic }, index) => {
        sessions.push({
          id: crypto.randomUUID(),
          subjectId: subject.id,
          subjectName: subject.name,
          topicId: topic.id,
          topicName: topic.name,
          date: dateKey(addDays(week, index ? 4 : 2)),
          time: index ? "18:30" : "19:00",
          duration: Math.min(20, duration),
          type: "Repaso",
          status: "Propuesta",
        });
      });
    }

    return { id: crypto.randomUUID(), weekStart: dateKey(week), generatedAt: Date.now(), sessions };
  },
  async save(plan) {
    return structuredClone(plan);
  },
  async exportPlan(plan, format) {
    if (format === "csv") {
      const rows = [
        ["Fecha", "Hora", "Materia", "Tema", "Tipo", "Duración", "Estado"],
        ...plan.sessions.map((session) => [
          session.date, session.time, session.subjectName, session.topicName,
          session.type, String(session.duration), session.status,
        ]),
      ];
      const csv = rows.map((row) => row.map((cell) => `"${cell.replaceAll('"', '""')}"`).join(",")).join("\n");
      return new Blob([csv], { type: "text/csv;charset=utf-8" });
    }
    const lines = ["Aslan - Plan semanal", `Semana del ${plan.weekStart}`, ""];
    plan.sessions.forEach((session) => lines.push(
      `${session.date} ${session.time} | ${session.subjectName} - ${session.topicName} | ${session.type} | ${session.duration} min | ${session.status}`,
    ));
    return new Blob([pdfFromLines(lines)], { type: "application/pdf" });
  },
};
