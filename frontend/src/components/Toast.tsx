import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react"
import "./toast.css"

export type ToastOptions = {
  /** Texto principal. Ej: "Exportación lista:" */
  message: string
  /** Parte subrayada al final del mensaje (opcional). Ej: "aslan-materias.pdf" */
  highlight?: string
  /** Texto del botón de acción (opcional). Ej: "Deshacer" */
  actionLabel?: string
  /** Se ejecuta al pulsar el botón de acción. El toast se cierra después. */
  onAction?: () => void
  /** Milisegundos antes de cerrarse solo. 0 = no se cierra solo. Por defecto 5000. */
  duration?: number
  /** Cambia el borde: "default" | "success" | "error". */
  tone?: "default" | "success" | "error"
}

type ActiveToast = ToastOptions & { id: number }

type ToastApi = {
  show: (options: ToastOptions) => void
  dismiss: () => void
}

const ToastContext = createContext<ToastApi | null>(null)

export function useToast(): ToastApi {
  const context = useContext(ToastContext)
  if (!context)
    throw new Error("useToast debe usarse dentro de <ToastProvider>")
  return context
}

function CloseIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
      <path
        d="m6 6 12 12M18 6 6 18"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
      />
    </svg>
  )
}

function ToastCard({
  toast,
  onClose,
}: {
  toast: ActiveToast
  onClose: () => void
}) {
  const [paused, setPaused] = useState(false)
  const duration = toast.duration ?? 5000

  useEffect(() => {
    if (!duration || paused) return
    const timer = window.setTimeout(onClose, duration)
    return () => window.clearTimeout(timer)
  }, [duration, paused, onClose])

  return (
    <aside
      className={`toast toast--${toast.tone ?? "default"}`}
      role={toast.tone === "error" ? "alert" : "status"}
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
    >
      <p className="toast-text">
        {toast.message}
        {toast.highlight && (
          <>
            {" "}
            <span className="toast-highlight">{toast.highlight}</span>
          </>
        )}
      </p>
      {toast.actionLabel && (
        <button
          type="button"
          className="toast-action"
          onClick={() => {
            toast.onAction?.()
            onClose()
          }}
        >
          {toast.actionLabel}
        </button>
      )}
      <button
        type="button"
        className="toast-close"
        onClick={onClose}
        aria-label="Cerrar notificación"
      >
        <CloseIcon />
      </button>
    </aside>
  )
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<ActiveToast | null>(null)
  const counter = useRef(0)

  const dismiss = useCallback(() => setToast(null), [])
  const show = useCallback((options: ToastOptions) => {
    counter.current += 1
    setToast({ ...options, id: counter.current })
  }, [])

  const api = useMemo(() => ({ show, dismiss }), [show, dismiss])

  return (
    <ToastContext.Provider value={api}>
      {children}
      {/* key = id: un toast nuevo reemplaza al anterior y reinicia el temporizador */}
      {toast && <ToastCard key={toast.id} toast={toast} onClose={dismiss} />}
    </ToastContext.Provider>
  )
}
