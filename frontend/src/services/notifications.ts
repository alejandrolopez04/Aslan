export type NotificationType = "Recordatorio" | "Motivación" | "Alerta" | "Sistema";
export type NotificationFrequency = "inmediata" | "diaria";

export type ContextNotification = {
  id: string;
  type: NotificationType;
  title: string;
  detail: string;
  createdAt: number;
  read: boolean;
  action: "reviews" | "plan" | "study" | "home";
};

export type RecommendationType =
  | "Descanso"
  | "Técnica"
  | "Motivación"
  | "Horario"
  | "Ambiente"
  | "Repaso";

export type ContextRecommendation = {
  id: string;
  type: RecommendationType;
  title: string;
  detail: string;
  reason: string;
  actionLabel: string;
  status: "pending" | "accepted" | "rejected" | "ignored";
  createdAt: number;
};

export type NotificationPreferences = {
  frequency: NotificationFrequency;
  reminders: boolean;
  motivation: boolean;
  alerts: boolean;
  system: boolean;
};

export interface NotificationGateway {
  list(userId: string): Promise<ContextNotification[]>;
  getPreferences(userId: string): Promise<NotificationPreferences>;
  savePreferences(userId: string, preferences: NotificationPreferences): Promise<NotificationPreferences>;
  markRead(id: string): Promise<void>;
  markAllRead(): Promise<void>;
  getRecommendations(userId: string): Promise<ContextRecommendation[]>;
  respondToRecommendation(id: string, response: "accepted" | "rejected" | "ignored"): Promise<ContextRecommendation>;
}

let notifications: ContextNotification[] = [];

let preferences: NotificationPreferences = {
  frequency: "inmediata",
  reminders: true,
  motivation: true,
  alerts: true,
  system: true,
};

let recommendations: ContextRecommendation[] = [];

export const notificationGateway: NotificationGateway = {
  async list(_userId) {
    return structuredClone(notifications).sort((a, b) => b.createdAt - a.createdAt);
  },
  async getPreferences(_userId) {
    return structuredClone(preferences);
  },
  async savePreferences(_userId, next) {
    preferences = structuredClone(next);
    return structuredClone(preferences);
  },
  async markRead(id) {
    const notification = notifications.find((item) => item.id === id);
    if (notification) notification.read = true;
  },
  async markAllRead() {
    notifications = notifications.map((notification) => ({ ...notification, read: true }));
  },
  async getRecommendations(_userId) {
    return structuredClone(recommendations).sort((a, b) => b.createdAt - a.createdAt);
  },
  async respondToRecommendation(id, response) {
    const recommendation = recommendations.find((item) => item.id === id);
    if (!recommendation) throw new Error("RECOMMENDATION_NOT_FOUND");
    recommendation.status = response;
    return structuredClone(recommendation);
  },
};
