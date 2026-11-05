const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:8080").replace(/\/$/, "");

export type ApiError = { codigo: string; mensaje: string };

export type ApiResult<T> =
  | { ok: true; status: number; data: T }
  | { ok: false; status: number; error: ApiError };

const NETWORK_ERROR: ApiError = {
  codigo: "SIN_CONEXION",
  mensaje: "No pudimos conectar con el servidor. Inténtalo de nuevo en unos minutos.",
};

export async function postJson<T>(path: string, body: unknown): Promise<ApiResult<T>> {
  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
  } catch {
    return { ok: false, status: 0, error: NETWORK_ERROR };
  }

  const payload = await response.json().catch(() => null);
  if (response.ok) return { ok: true, status: response.status, data: payload as T };
  const error = payload && typeof payload.codigo === "string" ? (payload as ApiError) : NETWORK_ERROR;
  return { ok: false, status: response.status, error };
}
