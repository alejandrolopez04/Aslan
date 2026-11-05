import { postJson, setAuthToken, type ApiError } from "./http";

export type User = {
  id: string;
  name: string;
  email: string;
};

export type AuthErrorCode =
  | "EMAIL_IN_USE"
  | "INVALID_CREDENTIALS"
  | "LOCKED"
  | "CURRENT_PASSWORD_INVALID"
  | "INVALID_DATA"
  | "SERVER_ERROR";

export type AuthResult<T> =
  | { ok: true; data: T }
  | {
      ok: false;
      code: AuthErrorCode;
      message?: string;
      attemptsRemaining?: number;
      lockedUntil?: number;
    };

type AccountSummary = { id: string; nombre: string; correo: string };
type StartedSession = { token: string; expiracion: string };
type TokenClaims = { sub: string; nombre: string; correo: string };

const ERROR_CODES: Record<string, AuthErrorCode> = {
  CORREO_EN_USO: "EMAIL_IN_USE",
  CONTRASENA_INSEGURA: "INVALID_DATA",
  DATOS_INVALIDOS: "INVALID_DATA",
  CREDENCIALES_INVALIDAS: "INVALID_CREDENTIALS",
  CUENTA_BLOQUEADA: "LOCKED",
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

// Perfil simulado hasta conectar HU04: guarda la cuenta que inició sesión.
const profiles = new Map<string, StoredUser>();

function normalizeEmail(email: string) {
  return email.trim().toLowerCase();
}

function toUser(account: AccountSummary): User {
  return { id: account.id, name: account.nombre, email: account.correo };
}

function decodeClaims(token: string): TokenClaims {
  const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
  const json = new TextDecoder().decode(Uint8Array.from(atob(payload), (c) => c.charCodeAt(0)));
  return JSON.parse(json) as TokenClaims;
}

function failure(error: ApiError): AuthResult<never> {
  const lockedUntil = error.detalles?.bloqueadaHasta;
  return {
    ok: false,
    code: ERROR_CODES[error.codigo] ?? "SERVER_ERROR",
    message: error.mensaje,
    lockedUntil: lockedUntil ? Date.parse(String(lockedUntil)) : undefined,
  };
}

function publicUser(user: StoredUser): User {
  const { password: _password, ...safeUser } = user;
  return safeUser;
}

export const authGateway: AuthGateway = {
  async register(name, email, password) {
    const result = await postJson<AccountSummary>("/api/auth/registro", {
      nombre: name,
      correo: email,
      contrasena: password,
    });
    if (!result.ok) return failure(result.error);
    return { ok: true, data: toUser(result.data) };
  },

  async login(email, password) {
    const result = await postJson<StartedSession>("/api/auth/login", {
      correo: email,
      contrasena: password,
    });
    if (!result.ok) return failure(result.error);

    setAuthToken(result.data.token);
    const claims = decodeClaims(result.data.token);
    const user = toUser({ id: claims.sub, nombre: claims.nombre, correo: claims.correo });
    profiles.set(user.id, { ...user, password });
    return { ok: true, data: user };
  },

  async updateProfile(userId, changes) {
    const user = profiles.get(userId);
    if (!user) {
      return { ok: false, code: "INVALID_CREDENTIALS" };
    }

    const emailChanged = normalizeEmail(changes.email) !== user.email;
    const passwordChanged = Boolean(changes.newPassword);

    if ((emailChanged || passwordChanged) && changes.currentPassword !== user.password) {
      return { ok: false, code: "CURRENT_PASSWORD_INVALID" };
    }

    user.name = changes.name.trim();
    user.email = normalizeEmail(changes.email);
    if (changes.newPassword) user.password = changes.newPassword;
    return { ok: true, data: publicUser(user) };
  },

  async logout() {
    await postJson<void>("/api/auth/logout");
    setAuthToken(null);
  },
};
