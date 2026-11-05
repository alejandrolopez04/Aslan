const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:8080").replace(/\/$/, "");

export type ApiError = {
  codigo: string;
  mensaje: string;
  detalles?: Record<string, string | string[] | number>;
};

export type ApiResult<T> =
  | { ok: true; status: number; data: T }
  | { ok: false; status: number; error: ApiError };

type Method = "GET" | "POST" | "PUT" | "PATCH";

const NETWORK_ERROR: ApiError = {
  codigo: "SIN_CONEXION",
  mensaje: "No pudimos conectar con el servidor. Inténtalo de nuevo en unos minutos.",
};

let authToken: string | null = null;
let onUnauthorized: (() => void) | null = null;

export function setAuthToken(token: string | null) {
  authToken = token;
}

export function setUnauthorizedHandler(handler: (() => void) | null) {
  onUnauthorized = handler;
}

async function request<T>(method: Method, path: string, body?: unknown): Promise<ApiResult<T>> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (authToken) headers.Authorization = `Bearer ${authToken}`;

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    return { ok: false, status: 0, error: NETWORK_ERROR };
  }

  const payload = response.status === 204 ? null : await response.json().catch(() => null);
  if (response.ok) return { ok: true, status: response.status, data: payload as T };
  if (response.status === 401 && !path.startsWith("/api/auth/")) onUnauthorized?.();
  const error = payload && typeof payload.codigo === "string" ? (payload as ApiError) : NETWORK_ERROR;
  return { ok: false, status: response.status, error };
}

export const getJson = <T>(path: string) => request<T>("GET", path);
export const postJson = <T>(path: string, body: unknown = {}) => request<T>("POST", path, body);
export const putJson = <T>(path: string, body: unknown) => request<T>("PUT", path, body);
export const patchJson = <T>(path: string, body: unknown) => request<T>("PATCH", path, body);

export class ApiFailure extends Error {
  constructor(readonly status: number, readonly error: ApiError) {
    super(error.mensaje);
  }
}

export function unwrap<T>(result: ApiResult<T>): T {
  if (!result.ok) throw new ApiFailure(result.status, result.error);
  return result.data;
}
