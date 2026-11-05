export type User = {
  id: string;
  name: string;
  email: string;
};

export type AuthErrorCode =
  | "EMAIL_IN_USE"
  | "INVALID_CREDENTIALS"
  | "LOCKED"
  | "CURRENT_PASSWORD_INVALID";

export type AuthResult<T> =
  | { ok: true; data: T }
  | {
      ok: false;
      code: AuthErrorCode;
      attemptsRemaining?: number;
      lockedUntil?: number;
    };

export type ProfileChanges = {
  name: string;
  email: string;
  currentPassword: string;
  newPassword?: string;
};

export interface AuthGateway {
  register(name: string, email: string, password: string): Promise<AuthResult<User>>;
  login(email: string, password: string): Promise<AuthResult<User>>;
  updateProfile(userId: string, changes: ProfileChanges): Promise<AuthResult<User>>;
  logout(): Promise<void>;
}

type StoredUser = User & { password: string };

const users: StoredUser[] = [
  {
    id: "usr_alejo",
    name: "Alejandro López",
    email: "alejo@aslan.app",
    password: "Alejo123.",
  },
];

const failedAttempts = new Map<string, { count: number; lockedUntil?: number }>();
const LOCK_AFTER = 3;
const LOCK_DURATION_MS = 30_000;

function normalizeEmail(email: string) {
  return email.trim().toLowerCase();
}

function publicUser(user: StoredUser): User {
  const { password: _password, ...safeUser } = user;
  return safeUser;
}

/**
 * Contrato de autenticación usado por la interfaz. Para integrar Spring Boot,
 * basta sustituir esta implementación por llamadas HTTP que devuelvan AuthResult.
 */
export const authGateway: AuthGateway = {
  async register(name, email, password) {
    const normalizedEmail = normalizeEmail(email);
    if (users.some((user) => user.email === normalizedEmail)) {
      return { ok: false, code: "EMAIL_IN_USE" };
    }

    const user: StoredUser = {
      id: crypto.randomUUID(),
      name: name.trim(),
      email: normalizedEmail,
      password,
    };
    users.push(user);
    return { ok: true, data: publicUser(user) };
  },

  async login(email, password) {
    const normalizedEmail = normalizeEmail(email);
    const attempt = failedAttempts.get(normalizedEmail);
    const now = Date.now();

    if (attempt?.lockedUntil && attempt.lockedUntil > now) {
      return { ok: false, code: "LOCKED", lockedUntil: attempt.lockedUntil };
    }

    const user = users.find(
      (candidate) =>
        candidate.email === normalizedEmail && candidate.password === password,
    );

    if (!user) {
      const nextCount = (attempt?.lockedUntil ? 0 : attempt?.count ?? 0) + 1;
      if (nextCount >= LOCK_AFTER) {
        const lockedUntil = now + LOCK_DURATION_MS;
        failedAttempts.set(normalizedEmail, { count: nextCount, lockedUntil });
        return { ok: false, code: "LOCKED", lockedUntil };
      }

      failedAttempts.set(normalizedEmail, { count: nextCount });
      return {
        ok: false,
        code: "INVALID_CREDENTIALS",
        attemptsRemaining: LOCK_AFTER - nextCount,
      };
    }

    failedAttempts.delete(normalizedEmail);
    return { ok: true, data: publicUser(user) };
  },

  async updateProfile(userId, changes) {
    const user = users.find((candidate) => candidate.id === userId);
    if (!user) {
      return { ok: false, code: "INVALID_CREDENTIALS" };
    }

    const emailChanged = normalizeEmail(changes.email) !== user.email;
    const passwordChanged = Boolean(changes.newPassword);

    if ((emailChanged || passwordChanged) && changes.currentPassword !== user.password) {
      return { ok: false, code: "CURRENT_PASSWORD_INVALID" };
    }

    const normalizedEmail = normalizeEmail(changes.email);
    if (
      emailChanged &&
      users.some(
        (candidate) =>
          candidate.id !== userId && candidate.email === normalizedEmail,
      )
    ) {
      return { ok: false, code: "EMAIL_IN_USE" };
    }

    user.name = changes.name.trim();
    user.email = normalizedEmail;
    if (changes.newPassword) user.password = changes.newPassword;
    return { ok: true, data: publicUser(user) };
  },

  async logout() {
    return Promise.resolve();
  },
};
