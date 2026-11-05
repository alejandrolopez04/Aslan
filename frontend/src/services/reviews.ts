export type ReviewStatus = "Programado" | "Pendiente de calificar" | "Completado";

export type Review = {
  id: string;
  subjectId: string;
  subjectName: string;
  topicId: string;
  topicName: string;
  dueDate: string;
  completedAt?: string;
  rating?: number;
  intervalDays: number;
  easeFactor: number;
  repetition: number;
  status: ReviewStatus;
};

export interface ReviewGateway {
  list(userId: string): Promise<Review[]>;
  scheduleInitial(input: Pick<Review, "subjectId" | "subjectName" | "topicId" | "topicName">): Promise<Review>;
  scheduleInitialMany(inputs: Array<Pick<Review, "subjectId" | "subjectName" | "topicId" | "topicName">>): Promise<Review[]>;
  rate(reviewId: string, rating: number): Promise<{ completed: Review; next: Review }>;
  leaveUnrated(reviewId: string): Promise<Review>;
}

function dateAfter(days: number) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString().slice(0, 10);
}

let reviews: Review[] = [];

function initialReview(input: Pick<Review, "subjectId" | "subjectName" | "topicId" | "topicName">): Review {
  return {
    ...input,
    id: crypto.randomUUID(),
    dueDate: dateAfter(1),
    intervalDays: 1,
    easeFactor: 2.5,
    repetition: 1,
    status: "Programado",
  };
}

export const reviewGateway: ReviewGateway = {
  async list(_userId) {
    return structuredClone(reviews);
  },
  async scheduleInitial(input) {
    const review = initialReview(input);
    reviews.push(review);
    return structuredClone(review);
  },
  async scheduleInitialMany(inputs) {
    const scheduled = inputs.map(initialReview);
    reviews.push(...scheduled);
    return structuredClone(scheduled);
  },
  async rate(reviewId, rating) {
    const current = reviews.find((review) => review.id === reviewId);
    if (!current) throw new Error("REVIEW_NOT_FOUND");
    const quality = Math.max(1, Math.min(5, rating));
    const nextEase = Math.max(
      1.3,
      current.easeFactor + 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02),
    );
    const nextInterval = quality <= 2
      ? 1
      : current.repetition <= 1
        ? 6
        : Math.max(1, Math.round(current.intervalDays * nextEase));

    current.status = "Completado";
    current.rating = quality;
    current.completedAt = new Date().toISOString().slice(0, 10);
    const next: Review = {
      id: crypto.randomUUID(),
      subjectId: current.subjectId,
      subjectName: current.subjectName,
      topicId: current.topicId,
      topicName: current.topicName,
      dueDate: dateAfter(nextInterval),
      intervalDays: nextInterval,
      easeFactor: Number(nextEase.toFixed(2)),
      repetition: quality <= 2 ? 1 : current.repetition + 1,
      status: "Programado",
    };
    reviews.push(next);
    return { completed: structuredClone(current), next: structuredClone(next) };
  },
  async leaveUnrated(reviewId) {
    const review = reviews.find((item) => item.id === reviewId);
    if (!review) throw new Error("REVIEW_NOT_FOUND");
    review.status = "Pendiente de calificar";
    return structuredClone(review);
  },
};
