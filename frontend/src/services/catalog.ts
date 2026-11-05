import { getJson, postJson, unwrap } from "./http";

export type SubjectColor = "red" | "blue" | "green" | "amber" | "purple";
export type SubjectIcon = "book" | "code" | "science" | "language" | "calculator";
export type TopicStatus = "Pendiente" | "En progreso" | "Completado";
export type TopicPriority = "Baja" | "Media" | "Alta";

export type Topic = {
  id: string;
  name: string;
  status: TopicStatus;
  priority: TopicPriority;
  dueDate?: string;
  notes: string;
  mastery?: number;
  scheduledReviews: number;
};

export type Subject = {
  id: string;
  name: string;
  color: SubjectColor;
  icon: SubjectIcon;
  archived: boolean;
  topics: Topic[];
};

export type SubjectInput = Pick<Subject, "name" | "color" | "icon">;
export type TopicInput = Omit<Topic, "id" | "mastery" | "scheduledReviews">;

export interface CatalogGateway {
  listSubjects(userId: string): Promise<Subject[]>;
  createSubject(userId: string, input: SubjectInput, confirmDuplicate?: boolean): Promise<Subject>;
  updateSubject(subjectId: string, input: SubjectInput): Promise<Subject>;
  setSubjectArchived(subjectId: string, archived: boolean): Promise<Subject>;
  deleteSubject(subjectId: string): Promise<void>;
  createTopic(subjectId: string, input: TopicInput): Promise<Topic>;
  updateTopic(subjectId: string, topicId: string, input: TopicInput): Promise<Topic>;
  deleteTopic(subjectId: string, topicId: string): Promise<void>;
  exportCatalog(subjects: Subject[], format: "pdf" | "csv" | "json"): Promise<Blob>;
}

type ApiTopic = {
  id: string;
  nombre: string;
  estado: "PENDIENTE" | "EN_PROGRESO" | "COMPLETADO";
  prioridad: "BAJA" | "MEDIA" | "ALTA";
  fechaLimite: string | null;
  notas: string;
};

type ApiSubject = {
  id: string;
  nombre: string;
  color: SubjectColor;
  icono: SubjectIcon;
  archivada: boolean;
  temas: ApiTopic[];
};

const STATUS: Record<ApiTopic["estado"], TopicStatus> = {
  PENDIENTE: "Pendiente",
  EN_PROGRESO: "En progreso",
  COMPLETADO: "Completado",
};
const PRIORITY: Record<ApiTopic["prioridad"], TopicPriority> = { BAJA: "Baja", MEDIA: "Media", ALTA: "Alta" };

function apiKey<K extends string, V>(map: Record<K, V>, value: V) {
  return (Object.keys(map) as K[]).find((key) => map[key] === value);
}

function toTopic(topic: ApiTopic): Topic {
  return {
    id: topic.id,
    name: topic.nombre,
    status: STATUS[topic.estado],
    priority: PRIORITY[topic.prioridad],
    dueDate: topic.fechaLimite ?? undefined,
    notes: topic.notas,
    scheduledReviews: 0,
  };
}

function toSubject(subject: ApiSubject): Subject {
  return {
    id: subject.id,
    name: subject.nombre,
    color: subject.color,
    icon: subject.icono,
    archived: subject.archivada,
    topics: subject.temas.map(toTopic),
  };
}

// HU14, HU15, HU17 y HU18 aún no tienen endpoint: sus cambios solo viven en esta copia local.
let subjects: Subject[] = [];

function findSubject(subjectId: string) {
  const subject = subjects.find((candidate) => candidate.id === subjectId);
  if (!subject) throw new Error("SUBJECT_NOT_FOUND");
  return subject;
}

function escapePdfText(value: string) {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").replace(/[()\\]/g, "\\$&");
}

function createSimplePdf(lines: string[]) {
  const content = [
    "BT",
    "/F1 12 Tf",
    "50 790 Td",
    ...lines.slice(0, 35).flatMap((line, index) => [
      index ? "0 -19 Td" : "",
      `(${escapePdfText(line).slice(0, 92)}) Tj`,
    ]).filter(Boolean),
    "ET",
  ].join("\n");
  const objects = [
    "<< /Type /Catalog /Pages 2 0 R >>",
    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] /Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
    `<< /Length ${content.length} >>\nstream\n${content}\nendstream`,
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
  pdf += `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF`;
  return pdf;
}

export const catalogGateway: CatalogGateway = {
  async listSubjects(_userId) {
    subjects = unwrap(await getJson<ApiSubject[]>("/api/materias")).map(toSubject);
    return structuredClone(subjects);
  },
  async createSubject(_userId, input, confirmDuplicate = false) {
    const subject = toSubject(unwrap(await postJson<ApiSubject>("/api/materias", {
      nombre: input.name,
      color: input.color,
      icono: input.icon,
      confirmarDuplicado: confirmDuplicate,
    })));
    subjects.push(subject);
    return structuredClone(subject);
  },
  async updateSubject(subjectId, input) {
    Object.assign(findSubject(subjectId), input);
    return structuredClone(findSubject(subjectId));
  },
  async setSubjectArchived(subjectId, archived) {
    findSubject(subjectId).archived = archived;
    return structuredClone(findSubject(subjectId));
  },
  async deleteSubject(subjectId) {
    subjects = subjects.filter((subject) => subject.id !== subjectId);
  },
  async createTopic(subjectId, input) {
    const topic = toTopic(unwrap(await postJson<ApiTopic>(`/api/materias/${subjectId}/temas`, {
      nombre: input.name,
      estado: apiKey(STATUS, input.status),
      prioridad: apiKey(PRIORITY, input.priority),
      fechaLimite: input.dueDate || null,
      notas: input.notes,
    })));
    findSubject(subjectId).topics.push(topic);
    return structuredClone(topic);
  },
  async updateTopic(subjectId, topicId, input) {
    const topic = findSubject(subjectId).topics.find((candidate) => candidate.id === topicId);
    if (!topic) throw new Error("TOPIC_NOT_FOUND");
    Object.assign(topic, input);
    return structuredClone(topic);
  },
  async deleteTopic(subjectId, topicId) {
    const subject = findSubject(subjectId);
    subject.topics = subject.topics.filter((topic) => topic.id !== topicId);
  },
  async exportCatalog(items, format) {
    if (format === "json") {
      return new Blob([JSON.stringify(items, null, 2)], { type: "application/json" });
    }
    if (format === "csv") {
      const rows = [["Materia", "Tema", "Estado", "Prioridad", "Fecha límite"]];
      items.forEach((subject) => {
        if (!subject.topics.length) rows.push([subject.name, "", "", "", ""]);
        subject.topics.forEach((topic) => rows.push([
          subject.name, topic.name, topic.status, topic.priority, topic.dueDate ?? "Sin vencimiento",
        ]));
      });
      const csv = rows.map((row) => row.map((cell) => `"${cell.replaceAll('"', '""')}"`).join(",")).join("\n");
      return new Blob([csv], { type: "text/csv;charset=utf-8" });
    }
    const lines = ["Aslan - Materias y temas", ""];
    items.forEach((subject) => {
      lines.push(`${subject.name} (${subject.archived ? "Archivada" : "Activa"})`);
      subject.topics.forEach((topic) => lines.push(`- ${topic.name} | ${topic.status} | ${topic.priority} | ${topic.dueDate ?? "Sin vencimiento"}`));
      lines.push("");
    });
    return new Blob([createSimplePdf(lines)], { type: "application/pdf" });
  },
};
