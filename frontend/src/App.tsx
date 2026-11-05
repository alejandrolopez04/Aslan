import {
  createElement,
  type ComponentProps,
  FormEvent,
  ReactNode,
  useEffect,
  useRef,
  useState,
  type PointerEvent as ReactPointerEvent,
} from "react"
// Lion icon by Icons8: https://icons8.com/icon/24970/lion
// Source: https://icons8.com
import lionIcon from "../lion-icon.png"
import checkIcon from "./imports/check.svg"
import addIcon from "./imports/add.svg"
import arrowBackIcon from "./imports/arrow-back.svg"
import boltIcon from "./imports/bolt.svg"
import calendarIcon from "./imports/calendar.svg"
import circleIcon from "./imports/circle.svg"
import deleteIcon from "./imports/delete.svg"
import dotsVertIcon from "./imports/dots-vert.svg"
import editLineIcon from "./imports/edit-line.svg"
import notificationsIcon from "./imports/notifications.svg"
import settingsIcon from "./imports/settings.svg"
import sidebarToggleIcon from "./imports/sidebar-toggle.svg"
import { authGateway, type AuthErrorCode, type User } from "./services/auth"
import {
  studyGateway,
  type SessionSummary,
  type StudySession,
  type StudySettings,
  type StudySuggestion,
} from "./services/study"
import {
  catalogGateway,
  type Subject,
  type SubjectColor,
  type SubjectIcon,
  type Topic,
  type TopicInput,
} from "./services/catalog"
import {
  plannerGateway,
  type PlanSession,
  type WeeklyPlan,
} from "./services/planner"
import { reviewGateway, type Review } from "./services/reviews"
import {
  analyticsGateway,
  type ActivitySummary,
  type ConcentrationPoint,
  type DashboardData,
} from "./services/analytics"
import {
  notificationGateway,
  type ContextNotification,
  type ContextRecommendation,
  type NotificationPreferences,
} from "./services/notifications"

import { useToast } from "./components/Toast"

type Route = "login" | "register" | "home" | "profile" | "subjects" | "reviews" | "notifications" | "plan" | "study" | "settings" | "summary"
type Notice = {
  type: "success" | "error" | "info"
  title: string
  detail: string
}

function ControlButton(props: ComponentProps<"button">) {
  return createElement("button", props)
}

function TextControl(props: ComponentProps<"input">) {
  return createElement("input", props)
}

function SparkIcon() {
  return (
    <svg
      aria-hidden="true"
      className="spark-icon"
      viewBox="0 0 24 24"
      fill="none"
    >
      <path
        d="M12 2.75c.7 4.5 3.25 7.05 7.75 7.75-4.5.7-7.05 3.25-7.75 7.75-.7-4.5-3.25-7.05-7.75-7.75C8.75 9.8 11.3 7.25 12 2.75Z"
        fill="currentColor"
      />
      <path
        d="M19.25 16.5c.22 1.4 1.1 2.28 2.5 2.5-1.4.22-2.28 1.1-2.5 2.5-.22-1.4-1.1-2.28-2.5-2.5 1.4-.22 2.28-1.1 2.5-2.5Z"
        fill="currentColor"
      />
    </svg>
  )
}

function CheckIcon() {
  return (
    <svg aria-hidden="true" viewBox="0 0 20 20" fill="none">
      <path
        d="m5 10 3.1 3L15 6.5"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

function EyeIcon({ hidden }: { hidden: boolean }) {
  return (
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
      <path
        d="M2.75 12s3.35-5 9.25-5 9.25 5 9.25 5-3.35 5-9.25 5-9.25-5-9.25-5Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle
        cx="12"
        cy="12"
        r="2.25"
        stroke="currentColor"
        strokeWidth="1.6"
      />
      {hidden && (
        <path
          d="m5 5 14 14"
          stroke="currentColor"
          strokeWidth="1.6"
          strokeLinecap="round"
        />
      )}
    </svg>
  )
}

function Brand() {
  return (
    <div className="brand">
      <span className="brand-mark">
        <img src={lionIcon} alt="" />
      </span>
      <span>Aslan</span>
    </div>
  )
}

function NoticeBox({ notice }: { notice: Notice }) {
  return (
    <div
      className={`notice notice--${notice.type}`}
      role={notice.type === "error" ? "alert" : "status"}
    >
      <span className="notice-icon">
        {notice.type === "success" ? (
          <CheckIcon />
        ) : notice.type === "error" ? (
          "!"
        ) : (
          "i"
        )}
      </span>
      <div>
        <p>{notice.title}</p>
        <span>{notice.detail}</span>
      </div>
    </div>
  )
}

function Field({
  id,
  label,
  type = "text",
  placeholder,
  value,
  onChange,
  error,
  autoComplete,
  trailing,
}: {
  id: string
  label: string
  type?: string
  placeholder: string
  value: string
  onChange: (value: string) => void
  error?: string
  autoComplete: string
  trailing?: ReactNode
}) {
  return (
    <div className="field-group">
      <label htmlFor={id}>{label}</label>
      <div className={`input-shell ${error ? "input-shell--error" : ""}`}>
        <input
          id={id}
          type={type}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          autoComplete={autoComplete}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
        />
        {trailing}
      </div>
      {error && (
        <p className="field-error" id={`${id}-error`}>
          {error}
        </p>
      )}
    </div>
  )
}

function PasswordField(props: {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
  error?: string
  placeholder?: string
  autoComplete?: string
}) {
  const [visible, setVisible] = useState(false)
  return (
    <Field
      {...props}
      type={visible ? "text" : "password"}
      placeholder={props.placeholder ?? "Ingresa tu contraseña"}
      autoComplete={props.autoComplete ?? "current-password"}
      trailing={
        <button
          className="visibility-button"
          type="button"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
        >
          <EyeIcon hidden={visible} />
        </button>
      }
    />
  )
}

function AuthLayout({
  children,
  mode,
}: {
  children: ReactNode
  mode: "login" | "register"
}) {
  return (
    <main className="app-shell">
      <section
        className="brand-panel"
        aria-label="Estudia de forma más inteligente"
      >
        <Brand />
        <div className="brand-copy">
          <p className="eyebrow">
            <span />
            Tu estudio, a tu ritmo
          </p>
          <h1>
            {mode === "login"
              ? "Tu progreso te está esperando."
              : "Un espacio que aprende contigo."}
          </h1>
          <p className="brand-description">
            Aslan reconoce tus rutinas y adapta cada sesión para ayudarte a
            concentrarte mejor, sin añadir ruido a tu día.
          </p>
        </div>
        <div className="insight-card">
          <div className="insight-icon">
            <SparkIcon />
          </div>
          <div>
            <p className="insight-label">
              {mode === "login"
                ? "Continúa donde lo dejaste"
                : "Una experiencia personal"}
            </p>
            <p className="insight-text">
              {mode === "login"
                ? "Tus preferencias y tu ritmo de estudio están listos para tu próxima sesión."
                : "Con el tiempo, tus sesiones se ajustarán a tus mejores momentos de concentración."}
            </p>
          </div>
        </div>
        <div className="ambient-shape ambient-shape--one" />
        <div className="ambient-shape ambient-shape--two" />
      </section>
      <section className="form-panel">
        <div className="mobile-brand">
          <Brand />
        </div>
        {children}
        <p className="privacy-note">
          Tus datos se usan únicamente para personalizar tu experiencia de
          estudio.
        </p>
      </section>
    </main>
  )
}

function LoginScreen({
  onLogin,
  onRegister,
}: {
  onLogin: (user: User) => void
  onRegister: () => void
}) {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [notice, setNotice] = useState<Notice | null>(null)
  const [lockedUntil, setLockedUntil] = useState(0)
  const [seconds, setSeconds] = useState(0)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!lockedUntil) return
    const update = () => {
      const remaining = Math.max(
        0,
        Math.ceil((lockedUntil - Date.now()) / 1000),
      )
      setSeconds(remaining)
      if (!remaining) {
        setLockedUntil(0)
        setNotice({
          type: "info",
          title: "Ya puedes intentarlo de nuevo",
          detail: "El bloqueo temporal ha finalizado.",
        })
      }
    }
    update()
    const timer = window.setInterval(update, 1000)
    return () => window.clearInterval(timer)
  }, [lockedUntil])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (lockedUntil) return
    setBusy(true)
    const result = await authGateway.login(email, password)
    setBusy(false)
    if (result.ok) return onLogin(result.data)
    if (result.code === "LOCKED") {
      setLockedUntil(result.lockedUntil ?? Date.now() + 30_000)
      setNotice({
        type: "error",
        title: "Acceso bloqueado temporalmente",
        detail:
          "Alcanzaste el límite de intentos. Espera antes de volver a intentarlo.",
      })
    } else {
      setNotice({
        type: "error",
        title: "Revisa tu correo y contraseña",
        detail: "Correo o contraseña inválida.",
      })
    }
  }

  return (
    <AuthLayout mode="login">
      <div className="form-container auth-form-short">
        <header className="login-header">
          <h2>Iniciar sesión</h2>
        </header>
        {notice?.type === "error" &&
          notice.title === "Revisa tu correo y contraseña" ? (
            <div className="login-credential-error" role="alert">
              <p className="login-invalid-detail">{notice.detail}</p>
            </div>
            ) : notice ? (
            <NoticeBox
              notice={
                lockedUntil
                ? {
                  ...notice,
                  detail: `${notice.detail} Podrás acceder en ${seconds} s.`,
                }
                : notice
              }
              />
          ) : null}
        <form onSubmit={handleSubmit} noValidate>
          <Field
            id="login-email"
            label="Correo electrónico"
            type="email"
            placeholder="tu@correo.com"
            value={email}
            onChange={setEmail}
            autoComplete="email"
            />
          <PasswordField
            id="login-password"
            label="Contraseña"
            value={password}
            onChange={setPassword}
            />
          <button
            className="submit-button"
            type="submit"
            disabled={busy || Boolean(lockedUntil)}
            >
            {lockedUntil
            ? `Espera ${seconds} s`
              : busy
              ? "Verificando..."
            : "Iniciar sesión"}
            <span aria-hidden="true">→</span>
          </button>
        </form>
        <p className="login-copy">
          ¿Aún no tienes una cuenta?{" "}
          <button type="button" className="text-button" onClick={onRegister}>
            Crear cuenta
          </button>
        </p>
      </div>
    </AuthLayout>
  )
}

function RegisterScreen({
  onLogin,
  onLoginScreen,
}: {
  onLogin: (user: User) => void
  onLoginScreen: () => void
}) {
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [notice, setNotice] = useState<Notice | null>(null)
  const [submitted, setSubmitted] = useState(false)
  const rules = {
    length: password.length >= 8,
    letter: /\p{L}/u.test(password),
    number: /\d/.test(password),
  }
  const passwordValid = Object.values(rules).every(Boolean)
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitted(true)
    if (!name.trim() || !emailValid || !passwordValid) {
      if (!passwordValid && password)
        setNotice({
          type: "error",
          title: "La contraseña aún no es válida",
          detail:
            "Debe tener al menos 8 caracteres, una letra y un número.",
        })
      return
    }
    const result = await authGateway.register(name, email, password)
    if (!result.ok) {
      setNotice(
        result.code === "EMAIL_IN_USE"
          ? {
              type: "error",
              title: "Este correo ya está en uso",
              detail:
                "Intenta iniciar sesión o utiliza otro correo para crear tu cuenta.",
            }
          : {
              type: "error",
              title: "No pudimos crear tu cuenta",
              detail: result.message ?? "Ocurrió un error inesperado.",
            },
      )
      return
    }
    setNotice({
      type: "success",
      title: "Tu cuenta fue creada con éxito",
      detail: `Te damos la bienvenida, ${result.data.name.split(" ")[0]}.`,
    })
  }

  return (
    <AuthLayout mode="register">
      <div className="form-container">
        <header>
          <h2>Crea tu cuenta</h2>
        </header>
        {notice && <NoticeBox notice={notice} />}
        <form onSubmit={handleSubmit} noValidate>
          <Field
            id="name"
            label="Nombre completo"
            value={name}
            onChange={setName}
            autoComplete="name"
            error={submitted && !name.trim() ? "Ingresa tu nombre." : undefined}
          />
          <Field
            id="email"
            label="Correo electrónico"
            type="email"
            placeholder="tu@correo.com"
            value={email}
            onChange={setEmail}
            autoComplete="email"
            error={
              submitted && !emailValid
                ? "Ingresa un correo electrónico válido."
                : undefined
            }
          />
          <PasswordField
            id="password"
            label="Contraseña"
            value={password}
            onChange={setPassword}
            autoComplete="new-password"
            placeholder="Crea una contraseña segura"
            error={
              submitted && !passwordValid
                ? "La contraseña no cumple todos los requisitos."
                : undefined
            }
          />
          <PasswordRequirements password={password} />
          <button className="submit-button" type="submit">
            Crear mi cuenta<span aria-hidden="true">→</span>
          </button>
        </form>
        <p className="login-copy">
          ¿Ya tienes una cuenta?{" "}
          <button type="button" className="text-button" onClick={onLoginScreen}>
            Inicia sesión
          </button>
        </p>
      </div>
    </AuthLayout>
  )
}

function PasswordRequirements({ password }: { password: string }) {
  const checks = [
    [password.length >= 8, "8 caracteres"],
    [/\p{L}/u.test(password), "Una letra"],
    [/\d/.test(password), "Un número"],
  ] as const
  return (
    <div className="requirements" aria-label="Requisitos de contraseña">
      <p>Tu contraseña debe incluir:</p>
      <div className="requirement-list">
        {checks.map(([valid, label]) => (
          <span key={label} className={valid ? "is-valid" : ""}>
            <i>
              <CheckIcon />
            </i>
            {label}
          </span>
        ))}
      </div>
    </div>
  )
}

type MenuIconName = "home" | "subjects" | "reviews" | "notifications" | "plan" | "study" | "timer" | "profile" | "logout"

function MenuIcon({ name }: { name: MenuIconName }) {
  if (name === "reviews") {
    return (
      <img
        aria-hidden="true"
        className="menu-icon menu-icon--navigation menu-icon--asset"
        src={checkIcon}
        alt=""
      />
    )
  }

  const paths: Record<MenuIconName, ReactNode> = {
    home: (
      <>
        <path d="m4 10 8-6 8 6" />
        <path d="M6 9v10h12V9M10 19v-6h4v6" />
      </>
    ),
    subjects: (
      <>
        <path d="M5 4h11a3 3 0 0 1 3 3v13H8a3 3 0 0 1-3-3V4Z" />
        <path d="M8 8h7M8 12h7M8 16h4" />
      </>
    ),
    reviews: (
      <>
        <path d="M19 8a7 7 0 1 0 1 6" />
        <path d="M19 3v5h-5M9 12l2 2 4-5" />
      </>
    ),
    notifications: (
      <>
        <path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 7h18s-3 0-3-7Z" />
        <path d="M10 20h4" />
      </>
    ),
    plan: (
      <>
        <rect x="4" y="5" width="16" height="15" rx="2" />
        <path d="M8 3v4M16 3v4M4 10h16M8 14h2M14 14h2" />
      </>
    ),
    study: (
      <>
        <path d="m3 8 9-5 9 5-9 5-9-5Z" />
        <path d="M7 11v5c3 2 7 2 10 0v-5M21 8v6" />
      </>
    ),
    timer: (
      <>
        <circle cx="12" cy="13" r="8" />
        <path d="M12 9v4l3 2M9 2h6" />
      </>
    ),
    profile: (
      <>
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21a8 8 0 0 1 16 0" />
      </>
    ),
    logout: (
      <>
        <path d="M10 5H5v14h5M14 8l4 4-4 4M8 12h10" />
      </>
    ),
  }
  return (
    <svg
      aria-hidden="true"
      className={`menu-icon ${
        name !== "logout" ? "menu-icon--navigation" : ""
      }`}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {paths[name]}
    </svg>
  )
}

function AppHeader({
  user,
  notificationCount = 0,
  activeSection,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
}: {
  user: User
  notificationCount?: number
  activeSection: "home" | "subjects" | "reviews" | "notifications" | "plan" | "study" | "settings" | "profile"
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return window.localStorage.getItem("aslan-sidebar-collapsed") === "true"
    } catch {
      return false
    }
  })
  const [sidebarWidth, setSidebarWidth] = useState(() => {
    try {
      const saved = Number(window.localStorage.getItem("aslan-sidebar-width"))
      return saved >= 210 && saved <= 360 ? saved : 250
    } catch {
      return 250
    }
  })
  const [notificationPopupOpen, setNotificationPopupOpen] = useState(false)
  const [headerNotifications, setHeaderNotifications] =
    useState<ContextNotification[]>([])
  const widthRef = useRef(sidebarWidth)
  const notificationPopupRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    notificationGateway.list(user.id).then(setHeaderNotifications)
  }, [notificationCount, user.id])

  useEffect(() => {
    if (!notificationPopupOpen) return
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (!notificationPopupRef.current?.contains(event.target as Node)) {
        setNotificationPopupOpen(false)
      }
    }
    document.addEventListener("pointerdown", closeOnOutsideClick)
    return () =>
      document.removeEventListener("pointerdown", closeOnOutsideClick)
  }, [notificationPopupOpen])

  useEffect(() => {
    widthRef.current = sidebarWidth
    const effectiveWidth = collapsed ? 0 : sidebarWidth
    document.documentElement.style.setProperty(
      "--sidebar-expanded-width",
      `${sidebarWidth}px`,
    )
    document.documentElement.style.setProperty(
      "--sidebar-width",
      `${effectiveWidth}px`,
    )
    try {
      window.localStorage.setItem("aslan-sidebar-width", String(sidebarWidth))
      window.localStorage.setItem("aslan-sidebar-collapsed", String(collapsed))
    } catch {
      // The menu still works when browser storage is unavailable.
    }
  }, [collapsed, sidebarWidth])

  function beginResize(event: ReactPointerEvent<HTMLButtonElement>) {
    if (collapsed) setCollapsed(false)
    const startX = event.clientX
    const startWidth = widthRef.current
    const handleMove = (moveEvent: PointerEvent) => {
      setSidebarWidth(
        Math.min(360, Math.max(210, startWidth + moveEvent.clientX - startX)),
      )
    }
    const handleEnd = () => {
      window.removeEventListener("pointermove", handleMove)
      window.removeEventListener("pointerup", handleEnd)
    }
    window.addEventListener("pointermove", handleMove)
    window.addEventListener("pointerup", handleEnd)
  }

  async function openContextNotification(notification: ContextNotification) {
    await notificationGateway.markRead(notification.id)
    setHeaderNotifications((current) =>
      current.map((item) =>
        item.id === notification.id ? { ...item, read: true } : item,
      ),
    )
    setNotificationPopupOpen(false)
    if (notification.action === "reviews") onReviews()
    else if (notification.action === "plan") onPlan()
    else if (notification.action === "study") onStudy()
    else onHome()
  }

  function openNotificationSettings() {
    try {
      window.sessionStorage.setItem("aslan-notification-tab", "settings")
    } catch {
      // Navigation still works when browser storage is unavailable.
    }
    setNotificationPopupOpen(false)
    onNotifications()
  }

  const unreadNotificationCount = headerNotifications.filter(
    (notification) => !notification.read,
  ).length

  return (
    <>
      <header
        className={`dashboard-header ${
          collapsed ? "dashboard-header--collapsed" : ""
        }`}
      >
        <div className="sidebar-brand-row">
          <button className="logo-button" type="button" onClick={onHome}>
            <Brand />
          </button>
          <div className="sidebar-top-actions">
            <div
              className="header-notification-control"
              ref={notificationPopupRef}
            >
              <button
                type="button"
                className={`header-notification-button ${
                  activeSection === "notifications" ? "is-active" : ""
                }`}
                onClick={async () => {
                  const items = await notificationGateway.list(user.id)
                  setHeaderNotifications(items)
                  setNotificationPopupOpen((current) => !current)
                }}
                aria-label="Mostrar notificaciones"
                aria-expanded={notificationPopupOpen}
                title="Notificaciones"
              >
                <img src={notificationsIcon} alt="" />
                {unreadNotificationCount > 0 && (
                  <span>{unreadNotificationCount}</span>
                )}
              </button>
              {notificationPopupOpen && (
                <div className="notification-popup">
                  <header>
                    <div>
                      <strong>Notificaciones</strong>
                    </div>
                    <button
                      type="button"
                      onClick={openNotificationSettings}
                      aria-label="Configurar notificaciones"
                      title="Configuración"
                    >
                      <img src={settingsIcon} alt="" />
                    </button>
                  </header>
                  <div className="notification-popup-list">
                    {headerNotifications.slice(0, 5).map((notification) => (
                      <button
                        type="button"
                        key={notification.id}
                        className={notification.read ? "" : "is-unread"}
                        onClick={() => openContextNotification(notification)}
                      >
                        <span
                          className={`context-icon context-icon--${notification.type
                            .toLowerCase()
                            .replace("ó", "o")}`}
                        >
                          {notification.type === "Recordatorio"
                            ? "R"
                            : notification.type === "Motivación"
                              ? "M"
                              : notification.type === "Alerta"
                                ? "!"
                                : "S"}
                        </span>
                        <span>
                          <strong>{notification.title}</strong>
                          <small>{notification.detail}</small>
                          <time>
                            {new Date(
                              notification.createdAt,
                            ).toLocaleDateString("es", {
                              day: "numeric",
                              month: "short",
                            })}
                          </time>
                        </span>
                        {!notification.read && <i />}
                      </button>
                    ))}
                    {!headerNotifications.length && (
                      <p>No tienes notificaciones todavía.</p>
                    )}
                  </div>
                  <footer>
                    <button
                      type="button"
                      onClick={() => {
                        setNotificationPopupOpen(false)
                        onNotifications()
                      }}
                    >
                      Ver historial completo
                    </button>
                  </footer>
                </div>
              )}
            </div>
            <button
              type="button"
              className="sidebar-toggle"
              onClick={() => setCollapsed(true)}
              aria-label="Ocultar menú"
              title="Ocultar menú"
            >
              <img src={sidebarToggleIcon} alt="" />
            </button>
          </div>
        </div>
        <nav aria-label="Navegación principal">
          <button
            type="button"
            className={activeSection === "home" ? "is-active" : ""}
            onClick={onHome}
            title="Inicio"
          >
            <MenuIcon name="home" />
            <span className="nav-label">Inicio</span>
          </button>
          <button
            type="button"
            className={activeSection === "subjects" ? "is-active" : ""}
            onClick={onSubjects}
            title="Materias"
          >
            <MenuIcon name="subjects" />
            <span className="nav-label">Materias</span>
          </button>
          <button
            type="button"
            className={activeSection === "reviews" ? "is-active" : ""}
            onClick={onReviews}
            title="Repasos"
          >
            <MenuIcon name="reviews" />
            <span className="nav-label">Repasos</span>
          </button>
          <button
            type="button"
            className={activeSection === "plan" ? "is-active" : ""}
            onClick={onPlan}
            title="Plan semanal"
          >
            <MenuIcon name="plan" />
            <span className="nav-label">Plan semanal</span>
          </button>
          <button
            type="button"
            className={activeSection === "study" ? "is-active" : ""}
            onClick={onStudy}
            title="Nueva sesión"
          >
            <MenuIcon name="study" />
            <span className="nav-label">Nueva sesión</span>
          </button>
          <button
            type="button"
            className={activeSection === "settings" ? "is-active" : ""}
            onClick={onSettings}
            title="Pomodoro"
          >
            <MenuIcon name="timer" />
            <span className="nav-label">Pomodoro</span>
          </button>
          <button
            type="button"
            className={activeSection === "profile" ? "is-active" : ""}
            onClick={onProfile}
            title="Mi perfil"
          >
            <MenuIcon name="profile" />
            <span className="nav-label">Mi perfil</span>
          </button>
        </nav>
        <div className="user-menu">
          <span>
            {user.name
              .split(" ")
              .map((part) => part[0])
              .slice(0, 2)
              .join("")}
          </span>
          <div>
            <strong>{user.name}</strong>
            <small>{user.email}</small>
          </div>
          <button type="button" onClick={onLogout} title="Cerrar sesión">
            <MenuIcon name="logout" />
            <span className="logout-label">Cerrar sesión</span>
          </button>
        </div>
        <button
          type="button"
          className="sidebar-resize-handle"
          onPointerDown={beginResize}
          aria-label="Cambiar tamaño del menú"
          title="Arrastra para cambiar el tamaño"
        />
      </header>
      {collapsed && (
        <button
          type="button"
          className="sidebar-show-button"
          onClick={() => setCollapsed(false)}
          aria-label="Mostrar menú"
          title="Mostrar menú"
        >
          <img src={sidebarToggleIcon} alt="" />
        </button>
      )}
    </>
  )
}

function ConcentrationChart({
  points,
}: {
  points: ConcentrationPoint[] | undefined
}) {
  const safePoints = Array.isArray(points) ? points : []
  const coordinates = safePoints
    .map((point, index) => {
      const x =
        safePoints.length === 1 ? 50 : (index / (safePoints.length - 1)) * 100
      const y = 100 - point.value
      return `${x},${y}`
    })
    .join(" ")
  return (
    <div className="concentration-chart">
      <svg
        viewBox="0 0 100 100"
        preserveAspectRatio="none"
        aria-label="Tendencia de concentración"
      >
        <path d="M0 25H100M0 50H100M0 75H100" className="chart-grid-lines" />
        {safePoints.length > 1 && (
          <polyline points={coordinates} className="chart-line" />
        )}
        {safePoints.map((point, index) => {
          const x =
            safePoints.length === 1
              ? 50
              : (index / (safePoints.length - 1)) * 100
          return (
            <circle
              key={`${point.label}-${index}`}
              cx={x}
              cy={100 - point.value}
              r="1.7"
            />
          )
        })}
      </svg>
      <div className="chart-labels">
        {safePoints.map((point) => (
          <span key={point.label}>{point.label}</span>
        ))}
      </div>
    </div>
  )
}

function DashboardExportDialog({
  data,
  onClose,
}: {
  data: DashboardData
  onClose: () => void
}) {
  const [format, setFormat] = useState<"pdf" | "csv" | "json">("pdf")
  const [notice, setNotice] = useState<Notice | null>(null)

  const toast = useToast()

  async function download() {
    if (!data.hasActivity) {
      toast.show({
        tone: "error",
        message: "Completa al menos una sesión antes de exportar tu tablero.",
        duration: 0,
      })
      return
    }
    const save = async () => {
      const blob = await analyticsGateway.exportDashboard(data, format)
      downloadFile(blob, `aslan-tablero.${format}`)
    }
    await save()
    onClose()
    toast.show({
      tone: "success",
      message: "Tablero exportado:",
      highlight: `aslan-tablero.${format}`,
      actionLabel: "Descargar de nuevo",
      onAction: save,
      duration: 0,
    })
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog catalog-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="dashboard-export-title"
      >
        <h2 id="dashboard-export-title">Exportar tablero</h2>
        <p>Incluye las métricas y datos visualizados actualmente.</p>
        {notice && <NoticeBox notice={notice} />}
        <div className="export-options">
          {(["pdf", "csv", "json"] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={format === item ? "is-selected" : ""}
              onClick={() => setFormat(item)}
            >
              <strong>{item.toUpperCase()}</strong>
              <span>
                {item === "pdf"
                  ? "Reporte visual"
                  : item === "csv"
                    ? "Datos tabulares"
                    : "Respaldo completo"}
              </span>
            </button>
          ))}
        </div>
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onClose}>
            Cerrar
          </button>
          <button type="button" className="submit-button" onClick={download}>
            Generar archivo
          </button>
        </div>
      </section>
    </div>
  )
}

function SummaryExportDialog({
  summaries,
  onClose,
}: {
  summaries: ActivitySummary[]
  onClose: () => void
}) {
  const [format, setFormat] = useState<"pdf" | "csv" | "json">("pdf")
  const [selected, setSelected] = useState<string[]>(
    summaries.map((summary) => summary.id),
  )
  const [notice, setNotice] = useState<Notice | null>(null)

  async function download() {
    const chosen = summaries.filter((summary) => selected.includes(summary.id))
    if (!chosen.length) {
      setNotice({
        type: "error",
        title: "No hay resúmenes disponibles",
        detail: "Selecciona al menos un resumen para exportar.",
      })
      return
    }
    const blob = await analyticsGateway.exportSummaries(chosen, format)
    downloadFile(blob, `aslan-resumenes.${format}`)
    setNotice({
      type: "success",
      title: "Resúmenes exportados",
      detail: `Se incluyeron ${chosen.length} resúmenes en el archivo.`,
    })
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog summary-export-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="summary-export-title"
      >
        <h2 id="summary-export-title">Exportar resúmenes</h2>
        <p>Selecciona uno o varios reportes diarios y semanales.</p>
        {notice && <NoticeBox notice={notice} />}
        <div className="summary-selection">
          {summaries.map((summary) => (
            <label key={summary.id}>
              <input
                type="checkbox"
                checked={selected.includes(summary.id)}
                onChange={() =>
                  setSelected((current) =>
                    current.includes(summary.id)
                      ? current.filter((id) => id !== summary.id)
                      : [...current, summary.id],
                  )
                }
              />
              <span>
                <strong>{summary.dateLabel}</strong>
                <small>
                  {summary.type === "daily"
                    ? "Resumen diario"
                    : "Resumen semanal"}{" "}
                  · {summary.minutes} min
                </small>
              </span>
            </label>
          ))}
          {!summaries.length && <p>No hay resúmenes disponibles todavía.</p>}
        </div>
        <div className="export-format-row">
          {(["pdf", "csv", "json"] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={format === item ? "is-selected" : ""}
              onClick={() => setFormat(item)}
            >
              {item.toUpperCase()}
            </button>
          ))}
        </div>
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onClose}>
            Cerrar
          </button>
          <button type="button" className="submit-button" onClick={download}>
            Exportar selección
          </button>
        </div>
      </section>
    </div>
  )
}

function downloadFile(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement("a")
  anchor.href = url
  anchor.download = filename
  anchor.click()
  URL.revokeObjectURL(url)
}

function HomeScreen({
  user,
  settings,
  subjects,
  plan,
  activityVersion,
  recommendations = [],
  onRecommendation,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
}: {
  user: User
  settings: StudySettings
  subjects: Subject[]
  plan: WeeklyPlan | null
  activityVersion: number
  recommendations: ContextRecommendation[]
  onRecommendation: (
    id: string,
    response: "accepted" | "rejected" | "ignored",
  ) => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  const [data, setData] = useState<DashboardData | null>(null)
  const [range, setRange] = useState<"weekly" | "monthly">("weekly")
  const [summaryType, setSummaryType] = useState<"daily" | "weekly">("daily")
  const [exportOpen, setExportOpen] = useState(false)
  const [summaryExportOpen, setSummaryExportOpen] = useState(false)

  useEffect(() => {
    analyticsGateway.getDashboard(user.id, subjects, plan).then(setData)
  }, [activityVersion, plan, subjects, user.id])

  if (!data)
    return (
      <main className="dashboard">
        <AppHeader
          user={user}
          activeSection="home"
          onHome={() => undefined}
          onSubjects={onSubjects}
          onReviews={onReviews}
          onNotifications={onNotifications}
          onPlan={onPlan}
          onStudy={onStudy}
          onSettings={onSettings}
          onProfile={onProfile}
          onLogout={onLogout}
        />
      </main>
    )

  const compliance = data.plannedSessions
    ? Math.round((data.completedSessions / data.plannedSessions) * 100)
    : 0
  const dashboardSubjects = Array.isArray(data.subjects) ? data.subjects : []
  const activityDays = Array.isArray(data.activityDays) ? data.activityDays : []
  const allSummaries = Array.isArray(data.summaries) ? data.summaries : []
  const safeRecommendations = Array.isArray(recommendations)
    ? recommendations
    : []
  const summaries = allSummaries.filter(
    (summary) => summary.type === summaryType,
  )
  const concentration =
    range === "weekly"
      ? Array.isArray(data.concentrationWeekly)
        ? data.concentrationWeekly
        : []
      : Array.isArray(data.concentrationMonthly)
        ? data.concentrationMonthly
        : []

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="home"
        onHome={() => undefined}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="performance-dashboard">
        <div className="dashboard-heading">
          <div>
            <p className="step-label">Tablero de rendimiento</p>
            <h1>Hola, {user.name.split(" ")[0]}</h1>
          </div>
          <div>
            <button
              className="secondary-button"
              type="button"
              onClick={() => setExportOpen(true)}
            >
              Exportar tablero
            </button>
            <button className="submit-button" type="button" onClick={onStudy}>
              Nueva sesión
            </button>
          </div>
        </div>

        <div className="metric-cards">
          <article>
            <span>Tiempo esta semana</span>
            <strong>
              {data.totalMinutesThisWeek}
              <small> min</small>
            </strong>
            <p>
              {data.hasActivity
                ? "Actividad acumulada"
                : "Sin actividad registrada"}
            </p>
          </article>
          <article>
            <span>Racha actual</span>
            <strong>
              {data.streakCurrent}
              <small> días</small>
            </strong>
            <p>Máxima: {data.streakMax} días</p>
          </article>
          <article>
            <span>Cumplimiento</span>
            <strong>
              {compliance}
              <small>%</small>
            </strong>
            <p>
              {data.completedSessions} de {data.plannedSessions} sesiones
            </p>
          </article>
          <article>
            <span>Próximo bloque</span>
            <strong>
              {settings.pomodoroMinutes}
              <small> min</small>
            </strong>
            <p>Pomodoro configurado</p>
          </article>
        </div>

        <div className="dashboard-grid">
          <article className="dashboard-panel concentration-panel">
            <header>
              <div>
                <span>Concentración</span>
                <h2>Tendencia de enfoque</h2>
              </div>
              <div className="range-tabs">
                <button
                  type="button"
                  className={range === "weekly" ? "is-active" : ""}
                  onClick={() => setRange("weekly")}
                >
                  Semana
                </button>
                <button
                  type="button"
                  className={range === "monthly" ? "is-active" : ""}
                  onClick={() => setRange("monthly")}
                >
                  Mes
                </button>
              </div>
            </header>
            {concentration.length ? (
              <ConcentrationChart points={concentration} />
            ) : (
              <div className="panel-empty">
                Completa sesiones para visualizar tu tendencia.
              </div>
            )}
          </article>
          <article className="dashboard-panel plan-comparison">
            <header>
              <div>
                <span>Plan semanal</span>
                <h2>Planificado vs. real</h2>
              </div>
              <button type="button" onClick={onPlan}>
                Ver plan
              </button>
            </header>
            <div className="comparison-number">
              <strong>{data.completedSessions}</strong>
              <span>de {data.plannedSessions} sesiones</span>
            </div>
            <div className="comparison-row">
              <span>Planificado</span>
              <i>
                <b style={{ width: data.plannedSessions ? "100%" : "0%" }} />
              </i>
              <strong>{data.plannedSessions}</strong>
            </div>
            <div className="comparison-row">
              <span>Realizado</span>
              <i>
                <b style={{ width: `${compliance}%` }} />
              </i>
              <strong>{data.completedSessions}</strong>
            </div>
            <p>
              {compliance >= 80
                ? "Vas en línea con lo planificado."
                : "Aún puedes ajustar la carga de esta semana."}
            </p>
          </article>
          <article className="dashboard-panel subjects-progress-panel">
            <header>
              <div>
                <span>Materias</span>
                <h2>Avance general</h2>
              </div>
              <button type="button" onClick={onSubjects}>
                Gestionar
              </button>
            </header>
            <div className="dashboard-subject-list">
              {dashboardSubjects.map((subject) => (
                <div
                  className={subject.critical ? "is-critical" : ""}
                  key={subject.subjectId}
                >
                  <div>
                    <strong>{subject.name}</strong>
                    {subject.critical && <span>Crítica</span>}
                  </div>
                  <i>
                    <b style={{ width: `${subject.progress}%` }} />
                  </i>
                  <small>{subject.progress}%</small>
                  {subject.overdue > 0 && (
                    <em>
                      {subject.overdue} vencido{subject.overdue > 1 ? "s" : ""}
                    </em>
                  )}
                </div>
              ))}
              {!dashboardSubjects.length && (
                <div className="panel-empty">
                  Crea materias para consultar su progreso.
                </div>
              )}
            </div>
          </article>
          <article className="dashboard-panel streak-panel">
            <header>
              <div>
                <span>Constancia</span>
                <h2>Racha de estudio</h2>
              </div>
            </header>
            <div className="streak-number">
              <strong>{data.streakCurrent}</strong>
              <span>días consecutivos</span>
            </div>
            <div className="activity-week">
              {activityDays.map((day) => (
                <div
                  key={day.date}
                  className={day.hadActivity ? "is-active" : ""}
                >
                  <i />
                  <span>
                    {new Date(`${day.date}T12:00:00`).toLocaleDateString("es", {
                      weekday: "narrow",
                    })}
                  </span>
                  <small>{day.hadActivity ? `${day.minutes}m` : "—"}</small>
                </div>
              ))}
            </div>
            <p>Tu mejor racha es de {data.streakMax} días.</p>
          </article>
        </div>

        <section className="recommendations-section">
          <header>
            <div>
              <span>Sugerencias contextuales</span>
              <h2>Recomendaciones para ti</h2>
            </div>
            <small>
              Tu respuesta ayuda a mejorar las próximas sugerencias.
            </small>
          </header>
          <div className="recommendation-grid">
            {safeRecommendations.map((recommendation) => (
              <article
                className={`context-recommendation context-recommendation--${recommendation.status}`}
                key={recommendation.id}
              >
                <div>
                  <span>{recommendation.type}</span>
                  {recommendation.status !== "pending" && (
                    <i>
                      {recommendation.status === "accepted"
                        ? "Aplicada"
                        : recommendation.status === "rejected"
                          ? "Rechazada"
                          : "Ignorada"}
                    </i>
                  )}
                </div>
                <h3>{recommendation.title}</h3>
                <p>{recommendation.detail}</p>
                <small>{recommendation.reason}</small>
                {recommendation.status === "pending" && (
                  <footer>
                    <button
                      type="button"
                      className="recommendation-ignore"
                      onClick={() =>
                        onRecommendation(recommendation.id, "ignored")
                      }
                    >
                      Ahora no
                    </button>
                    <button
                      type="button"
                      className="recommendation-reject"
                      onClick={() =>
                        onRecommendation(recommendation.id, "rejected")
                      }
                    >
                      No me sirve
                    </button>
                    <button
                      type="button"
                      className="recommendation-accept"
                      onClick={() =>
                        onRecommendation(recommendation.id, "accepted")
                      }
                    >
                      {recommendation.actionLabel}
                    </button>
                  </footer>
                )}
              </article>
            ))}
          </div>
        </section>

        <section className="summaries-section">
          <header>
            <div>
              <span>Generados por Aslan</span>
              <h2>Resúmenes de actividad</h2>
            </div>
            <div>
              <button
                type="button"
                className="secondary-button"
                onClick={() => setSummaryExportOpen(true)}
              >
                Exportar resúmenes
              </button>
            </div>
          </header>
          <div className="catalog-tabs summary-tabs">
            <button
              type="button"
              className={summaryType === "daily" ? "is-active" : ""}
              onClick={() => setSummaryType("daily")}
            >
              Diarios
            </button>
            <button
              type="button"
              className={summaryType === "weekly" ? "is-active" : ""}
              onClick={() => setSummaryType("weekly")}
            >
              Semanales
            </button>
          </div>
          <div className="summary-grid">
            {summaries.map((summary) => (
              <article key={summary.id}>
                <div>
                  <span>{summary.dateLabel}</span>
                  <small>
                    {summary.periodStart} · {summary.periodEnd}
                  </small>
                </div>
                <p>{summary.insight}</p>
                <footer>
                  <span>
                    <strong>{summary.minutes}</strong> min
                  </span>
                  <span>
                    <strong>{summary.sessions}</strong> sesiones
                  </span>
                  <span>
                    <strong>{summary.concentration}%</strong> concentración
                  </span>
                  <span>
                    <strong>{summary.completedTopics}</strong> temas
                  </span>
                </footer>
              </article>
            ))}
            {!summaries.length && (
              <div className="summary-empty">
                <h3>No hay resúmenes disponibles todavía</h3>
                <p>
                  {summaryType === "daily"
                    ? "Los días sin actividad se registran, pero no generan un resumen diario."
                    : "El primer resumen se generará al finalizar la semana."}
                </p>
              </div>
            )}
          </div>
        </section>
      </section>
      {exportOpen && (
        <DashboardExportDialog
          data={data}
          onClose={() => setExportOpen(false)}
        />
      )}
      {summaryExportOpen && (
        <SummaryExportDialog
          summaries={allSummaries}
          onClose={() => setSummaryExportOpen(false)}
        />
      )}
    </main>
  )
}

function NotificationCenterScreen({
  user,
  notifications,
  preferences,
  onPreferencesChange,
  onRefresh,
  onNavigate,
  onHome,
  onSubjects,
  onReviews,
  onPlan,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
}: {
  user: User
  notifications: ContextNotification[]
  preferences: NotificationPreferences
  onPreferencesChange: (preferences: NotificationPreferences) => void
  onRefresh: () => void
  onNavigate: (route: ContextNotification["action"]) => void
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  const [tab, setTab] = useState<"history" | "settings">(() => {
    try {
      const requestedTab = window.sessionStorage.getItem(
        "aslan-notification-tab",
      )
      window.sessionStorage.removeItem("aslan-notification-tab")
      return requestedTab === "settings" ? "settings" : "history"
    } catch {
      return "history"
    }
  })
  const [notice, setNotice] = useState<Notice | null>(null)

  async function savePreferences(next: NotificationPreferences) {
    const saved = await notificationGateway.savePreferences(user.id, next)
    onPreferencesChange(saved)
    setNotice({
      type: "success",
      title: "Preferencias actualizadas",
      detail:
        saved.frequency === "inmediata"
          ? "Recibirás cada aviso en el momento en que se genere."
          : "Tus avisos se agruparán en un resumen diario.",
    })
  }

  async function openNotification(notification: ContextNotification) {
    await notificationGateway.markRead(notification.id)
    onRefresh()
    onNavigate(notification.action)
  }

  const enabledKey: Record<ContextNotification["type"], keyof NotificationPreferences> =
    {
      Recordatorio: "reminders",
      Motivación: "motivation",
      Alerta: "alerts",
      Sistema: "system",
    }

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="notifications"
        notificationCount={notifications.filter((item) => !item.read).length}
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={() => undefined}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="notifications-page">
        <div className="catalog-heading">
          <div>
            <p className="step-label">Centro de avisos</p>
            <h1>Notificaciones</h1>
            <p>Recordatorios, alertas y mensajes contextuales de Aslan.</p>
          </div>
          {notifications.some((item) => !item.read) && (
            <div>
              <button
                type="button"
                className="secondary-button"
                onClick={async () => {
                  await notificationGateway.markAllRead()
                  onRefresh()
                }}
              >
                Marcar todo como leído
              </button>
            </div>
          )}
        </div>
        {notice && <NoticeBox notice={notice} />}
        <div className="catalog-tabs">
          <button
            type="button"
            className={tab === "history" ? "is-active" : ""}
            onClick={() => setTab("history")}
          >
            Historial
          </button>
          <button
            type="button"
            className={tab === "settings" ? "is-active" : ""}
            onClick={() => setTab("settings")}
          >
            Configuración
          </button>
        </div>
        {tab === "history" ? (
          <div className="notification-history">
            {notifications.map((notification) => (
              <button
                key={notification.id}
                type="button"
                className={`notification-history-item ${
                  notification.read ? "" : "is-unread"
                }`}
                onClick={() => openNotification(notification)}
              >
                <span
                  className={`context-icon context-icon--${notification.type.toLowerCase().replace("ó", "o")}`}
                >
                  {notification.type === "Recordatorio"
                    ? "R"
                    : notification.type === "Motivación"
                      ? "M"
                      : notification.type === "Alerta"
                        ? "!"
                        : "S"}
                </span>
                <span className="notification-copy">
                  <span>{notification.type}</span>
                  <strong>{notification.title}</strong>
                  <small>{notification.detail}</small>
                </span>
                <time>
                  {new Date(notification.createdAt).toLocaleDateString("es", {
                    day: "numeric",
                    month: "short",
                  })}{" "}
                  ·{" "}
                  {new Date(notification.createdAt).toLocaleTimeString("es", {
                    hour: "2-digit",
                    minute: "2-digit",
                  })}
                </time>
                {!notification.read && <i />}
              </button>
            ))}
          </div>
        ) : (
          <div className="notification-settings-card">
            <div className="notification-setting-section">
              <div>
                <h2>Frecuencia</h2>
                <p>
                  Decide si quieres recibir cada aviso o un único resumen
                  agrupado.
                </p>
              </div>
              <div className="frequency-options">
                <button
                  type="button"
                  className={
                    preferences.frequency === "inmediata" ? "is-selected" : ""
                  }
                  onClick={() =>
                    savePreferences({ ...preferences, frequency: "inmediata" })
                  }
                >
                  <strong>Inmediata</strong>
                  <span>Cada aviso al generarse</span>
                </button>
                <button
                  type="button"
                  className={
                    preferences.frequency === "diaria" ? "is-selected" : ""
                  }
                  onClick={() =>
                    savePreferences({ ...preferences, frequency: "diaria" })
                  }
                >
                  <strong>Resumen diario</strong>
                  <span>Todos los avisos juntos</span>
                </button>
              </div>
            </div>
            <div className="profile-divider" />
            <div className="notification-setting-section">
              <div>
                <h2>Tipos de aviso</h2>
                <p>Controla qué mensajes puede enviarte Aslan.</p>
              </div>
              <div className="type-settings">
                {([
                  "Recordatorio",
                  "Motivación",
                  "Alerta",
                  "Sistema",
                ] as const).map((type) => {
                  const key = enabledKey[type]
                  const checked = preferences[key] as boolean
                  return (
                    <div key={type}>
                      <span>
                        <strong>{type}</strong>
                        <small>
                          {type === "Recordatorio"
                            ? "Sesiones y repasos próximos"
                            : type === "Motivación"
                              ? "Rachas y logros personales"
                              : type === "Alerta"
                                ? "Inactividad o eventos críticos"
                                : "Cambios realizados por el sistema"}
                        </small>
                      </span>
                      <button
                        type="button"
                        role="switch"
                        aria-checked={checked}
                        className={`switch ${checked ? "switch--on" : ""}`}
                        onClick={() =>
                          savePreferences({ ...preferences, [key]: !checked })
                        }
                      >
                        <span />
                      </button>
                    </div>
                  )
                })}
              </div>
            </div>
          </div>
        )}
      </section>
    </main>
  )
}

function ProfileScreen({
  user,
  onUpdated,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onStudy,
  onSettings,
  onLogout,
}: {
  user: User
  onUpdated: (user: User) => void
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onLogout: () => void
}) {
  const [name, setName] = useState(user.name)
  const [email, setEmail] = useState(user.email)
  const [currentPassword, setCurrentPassword] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [notice, setNotice] = useState<Notice | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const result = await authGateway.updateProfile(user.id, {
      name,
      email,
      currentPassword,
      newPassword: newPassword || undefined,
    })
    if (!result.ok) {
      const messages: Record<AuthErrorCode, Notice> = {
        CURRENT_PASSWORD_INVALID: {
          type: "error",
          title: "La contraseña actual es incorrecta",
          detail:
            "No pudimos confirmar tu identidad. Revisa la contraseña e inténtalo de nuevo.",
        },
        EMAIL_IN_USE: {
          type: "error",
          title: "Este correo no está disponible",
          detail: "Ya existe otra cuenta asociada a este correo electrónico.",
        },
        INVALID_CREDENTIALS: {
          type: "error",
          title: "No fue posible guardar",
          detail: "Tu sesión ya no es válida. Vuelve a iniciar sesión.",
        },
        LOCKED: {
          type: "error",
          title: "Acceso temporalmente bloqueado",
          detail: "Espera antes de intentarlo nuevamente.",
        },
        INVALID_DATA: {
          type: "error",
          title: "Revisa los datos ingresados",
          detail: result.message ?? "Los datos enviados no son válidos.",
        },
        SERVER_ERROR: {
          type: "error",
          title: "No fue posible guardar",
          detail: result.message ?? "Ocurrió un error inesperado.",
        },
      }
      setNotice(messages[result.code])
      return
    }
    onUpdated(result.data)
    setCurrentPassword("")
    setNewPassword("")
    setNotice({
      type: "success",
      title: "Perfil actualizado",
      detail: "Tus cambios se guardaron correctamente.",
    })
  }

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="profile"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={() => undefined}
        onLogout={onLogout}
      />
      <section className="profile-page">
        <button className="back-button" type="button" onClick={onHome}>
          ← Volver al inicio
        </button>
        <div className="profile-heading">
          <div>
            <p className="step-label">Configuración de cuenta</p>
            <h1>Mi perfil</h1>
            <p>Mantén actualizados tus datos personales y de acceso.</p>
          </div>
          <span className="profile-avatar">
            {user.name
              .split(" ")
              .map((part) => part[0])
              .slice(0, 2)
              .join("")}
          </span>
        </div>
        <div className="profile-card">
          {notice && <NoticeBox notice={notice} />}
          <form onSubmit={handleSubmit}>
            <div className="profile-section">
              <div className="section-copy">
                <h2>Información personal</h2>
                <p>Datos que usaremos para identificar tu cuenta.</p>
              </div>
              <div className="section-fields">
                <Field
                  id="profile-name"
                  label="Nombre completo"
                  placeholder="Tu nombre"
                  value={name}
                  onChange={setName}
                  autoComplete="name"
                />
                <Field
                  id="profile-email"
                  label="Correo electrónico"
                  type="email"
                  placeholder="tu@correo.com"
                  value={email}
                  onChange={setEmail}
                  autoComplete="email"
                />
              </div>
            </div>
            <div className="profile-divider" />
            <div className="profile-section">
              <div className="section-copy">
                <h2>Seguridad</h2>
                <p>
                  Para cambiar tu correo o contraseña, confirma primero tu
                  contraseña actual.
                </p>
              </div>
              <div className="section-fields">
                <PasswordField
                  id="current-password"
                  label="Contraseña actual"
                  value={currentPassword}
                  onChange={setCurrentPassword}
                />
                <PasswordField
                  id="new-password"
                  label="Nueva contraseña (opcional)"
                  value={newPassword}
                  onChange={setNewPassword}
                  autoComplete="new-password"
                  placeholder="Deja en blanco para no cambiarla"
                />
                {newPassword && <PasswordRequirements password={newPassword} />}
              </div>
            </div>
            <div className="profile-actions">
              <button
                type="button"
                className="secondary-button"
                onClick={onHome}
              >
                Cancelar
              </button>
              <button type="submit" className="submit-button">
                Guardar cambios
              </button>
            </div>
          </form>
        </div>
      </section>
    </main>
  )
}

function SubjectGlyph({ icon }: { icon: SubjectIcon }) {
  const paths: Record<SubjectIcon, ReactNode> = {
    book: (
      <>
        <path d="M5 4.5h8a3 3 0 0 1 3 3V20H8a3 3 0 0 1-3-3V4.5Z" />
        <path d="M16 7.5h3V20h-3M8 8h5M8 12h5" />
      </>
    ),
    code: (
      <>
        <path d="m9 7-5 5 5 5M15 7l5 5-5 5M13 4l-2 16" />
      </>
    ),
    science: (
      <>
        <path d="M9 3h6M10 3v6l-5 9a2 2 0 0 0 1.8 3h10.4a2 2 0 0 0 1.8-3l-5-9V3M8 15h8" />
      </>
    ),
    language: (
      <>
        <circle cx="12" cy="12" r="9" />
        <path d="M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3-3 15 0 18" />
      </>
    ),
    calculator: (
      <>
        <rect x="5" y="3" width="14" height="18" rx="2" />
        <path d="M8 6h8v4H8zM8 14h.01M12 14h.01M16 14h.01M8 18h.01M12 18h.01M16 18h.01" />
      </>
    ),
  }
  return (
    <svg
      aria-hidden="true"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {paths[icon]}
    </svg>
  )
}

function subjectProgress(subject: Subject) {
  if (!subject.topics.length) return 0
  return Math.round(
    (subject.topics.filter((topic) => topic.status === "Completado").length /
      subject.topics.length) *
      100,
  )
}

function topicTiming(topic: Topic) {
  if (!topic.dueDate || topic.status === "Completado") return null
  const days = Math.ceil(
    (new Date(`${topic.dueDate}T23:59:59`).getTime() - Date.now()) / 86_400_000,
  )
  if (days < 0) return "overdue"
  if (days <= 7) return "risk"
  return null
}

function topicDuePresentation(topic: Topic) {
  if (!topic.dueDate) {
    return { label: "Sin vencimiento", tone: "none" }
  }
  const dueDate = new Date(`${topic.dueDate}T12:00:00`)
  const today = new Date()
  today.setHours(12, 0, 0, 0)
  const days = Math.round((dueDate.getTime() - today.getTime()) / 86_400_000)
  if (days < 0) return { label: "Vencido", tone: "overdue" }
  if (days === 0) return { label: "Hoy", tone: "today" }
  if (days === 1) return { label: "Mañana", tone: "tomorrow" }
  if (days <= 7) return { label: `En ${days} días`, tone: "soon" }
  return {
    label: dueDate.toLocaleDateString("es", {
      day: "numeric",
      month: "short",
    }),
    tone: "future",
  }
}

function CalendarPicker({
  value,
  onChange,
  markedDates = [],
  iconOnly = false,
  inline = false,
}: {
  value: string
  onChange: (date: string) => void
  markedDates?: string[]
  iconOnly?: boolean
  inline?: boolean
}) {
  const [open, setOpen] = useState(inline)
  const [placement, setPlacement] = useState<"below" | "above">("below")
  const [visibleMonth, setVisibleMonth] = useState(() => {
    const initial = value ? new Date(`${value}T12:00:00`) : new Date()
    return new Date(initial.getFullYear(), initial.getMonth(), 1)
  })
  const pickerRef = useRef<HTMLDivElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!open || inline) return
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (!pickerRef.current?.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener("pointerdown", closeOnOutsideClick)
    return () =>
      document.removeEventListener("pointerdown", closeOnOutsideClick)
  }, [inline, open])

  function dateKey(date: Date) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
      2,
      "0",
    )}-${String(date.getDate()).padStart(2, "0")}`
  }

  const firstDayOffset = (visibleMonth.getDay() + 6) % 7
  const calendarStart = new Date(
    visibleMonth.getFullYear(),
    visibleMonth.getMonth(),
    1 - firstDayOffset,
  )
  const days = Array.from({ length: 42 }, (_, index) => {
    const date = new Date(calendarStart)
    date.setDate(calendarStart.getDate() + index)
    return date
  })
  const monthLabel = visibleMonth.toLocaleDateString("es", {
    month: "short",
    year: "numeric",
  })
  const selectedLabel = value
    ? new Date(`${value}T12:00:00`).toLocaleDateString("es", {
        day: "numeric",
        month: "short",
        year: "numeric",
      })
    : "Seleccionar fecha"
  const todayKey = dateKey(new Date())

  return (
    <div
      className={`calendar-picker ${iconOnly ? "calendar-picker--icon" : ""} ${
        inline ? "calendar-picker--inline" : ""
      }`}
      ref={pickerRef}
    >
      <button
        type="button"
        className="calendar-picker-trigger"
        ref={triggerRef}
        onClick={() => {
          if (!open) {
            const bounds = triggerRef.current?.getBoundingClientRect()
            const spaceBelow = bounds
              ? window.innerHeight - bounds.bottom
              : window.innerHeight
            setPlacement(
              bounds && spaceBelow < 325 && bounds.top > spaceBelow
                ? "above"
                : "below",
            )
          }
          setOpen((current) => !current)
        }}
        aria-haspopup="dialog"
        aria-expanded={open}
        aria-label={iconOnly ? "Modificar fecha límite" : undefined}
        title={iconOnly ? "Modificar fecha límite" : undefined}
      >
        <img src={calendarIcon} alt="" />
        {!iconOnly && <span>{selectedLabel}</span>}
      </button>
      {open && (
        <div
          className={`calendar-popover calendar-popover--${placement}`}
          role="dialog"
          aria-label="Calendario"
        >
          <header>
            <strong>{monthLabel}</strong>
            <div>
              <button
                type="button"
                onClick={() =>
                  setVisibleMonth(
                    (current) =>
                      new Date(
                        current.getFullYear(),
                        current.getMonth() - 1,
                        1,
                      ),
                  )
                }
                aria-label="Mes anterior"
              >
                ‹
              </button>
              <button
                type="button"
                className="calendar-today-button"
                onClick={() => {
                  const today = new Date()
                  setVisibleMonth(
                    new Date(today.getFullYear(), today.getMonth(), 1),
                  )
                }}
                aria-label="Ir al mes actual"
              >
                <span />
              </button>
              <button
                type="button"
                onClick={() =>
                  setVisibleMonth(
                    (current) =>
                      new Date(
                        current.getFullYear(),
                        current.getMonth() + 1,
                        1,
                      ),
                  )
                }
                aria-label="Mes siguiente"
              >
                ›
              </button>
            </div>
          </header>
          <div className="calendar-weekdays">
            {["L", "M", "X", "J", "V", "S", "D"].map((day) => (
              <span key={day}>{day}</span>
            ))}
          </div>
          <div className="calendar-days">
            {days.map((date) => {
              const key = dateKey(date)
              const inCurrentMonth = date.getMonth() === visibleMonth.getMonth()
              return (
                <button
                  type="button"
                  key={key}
                  className={[
                    inCurrentMonth ? "" : "is-outside",
                    key === value ? "is-selected" : "",
                    key === todayKey ? "is-today" : "",
                    markedDates.includes(key) ? "has-event" : "",
                  ]
                    .filter(Boolean)
                    .join(" ")}
                  onClick={() => {
                    onChange(key)
                    if (!inline) setOpen(false)
                  }}
                >
                  <span>{date.getDate()}</span>
                  {markedDates.includes(key) && <i />}
                </button>
              )
            })}
          </div>
          <footer>
            <button
              type="button"
              onClick={() => {
                onChange("")
                if (!inline) setOpen(false)
              }}
            >
              Sin fecha límite
            </button>
            <span>
              {new Date(
                visibleMonth.getFullYear(),
                visibleMonth.getMonth() + 1,
                1,
              ).toLocaleDateString("es", { month: "short" })}
            </span>
          </footer>
        </div>
      )}
    </div>
  )
}

function SubjectFormDialog({
  subjects,
  editing,
  onClose,
  onSaved,
}: {
  subjects: Subject[]
  editing?: Subject
  onClose: () => void
  onSaved: () => void
}) {
  const [name, setName] = useState(editing?.name ?? "")
  const [color, setColor] = useState<SubjectColor>(editing?.color ?? "red")
  const [icon, setIcon] = useState<SubjectIcon>(editing?.icon ?? "book")
  const [error, setError] = useState("")
  const colors: SubjectColor[] = ["red", "blue", "green", "amber", "purple"]
  const icons: SubjectIcon[] = [
    "book",
    "code",
    "science",
    "language",
    "calculator",
  ]

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!name.trim()) {
      setError("Ingresa un nombre para la materia.")
      return
    }
    if (
      subjects.some(
        (subject) =>
          subject.id !== editing?.id &&
          subject.name.toLowerCase() === name.trim().toLowerCase(),
      )
    ) {
      setError("Ya existe una materia con este nombre.")
      return
    }
    if (editing)
      await catalogGateway.updateSubject(editing.id, {
        name: name.trim(),
        color,
        icon,
      })
    else
      await catalogGateway.createSubject("current-user", {
        name: name.trim(),
        color,
        icon,
      })
    onSaved()
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog catalog-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="subject-dialog-title"
      >
        <h2 id="subject-dialog-title">
          {editing ? "Editar materia" : "Nueva materia"}
        </h2>
        <p>Personaliza cómo aparecerá en tu espacio de estudio.</p>
        <form onSubmit={save}>
          <Field
            id="subject-name"
            label="Nombre"
            placeholder=""
            value={name}
            onChange={(value) => {
              setName(value)
              setError("")
            }}
            autoComplete="off"
            error={error || undefined}
          />
          <div className="field-group">
            <label>Color</label>
            <div className="choice-row color-choices">
              {colors.map((item) => (
                <button
                  key={item}
                  type="button"
                  aria-label={`Color ${item}`}
                  className={`subject-color subject-color--${item} ${
                    color === item ? "is-selected" : ""
                  }`}
                  onClick={() => setColor(item)}
                />
              ))}
            </div>
          </div>
          <div className="field-group">
            <label>Ícono</label>
            <div className="choice-row icon-choices">
              {icons.map((item) => (
                <button
                  key={item}
                  type="button"
                  aria-label={`Ícono ${item}`}
                  className={icon === item ? "is-selected" : ""}
                  onClick={() => setIcon(item)}
                >
                  <SubjectGlyph icon={item} />
                </button>
              ))}
            </div>
          </div>
          <div className="dialog-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={onClose}
            >
              Cancelar
            </button>
            <button type="submit" className="submit-button">
              {editing ? "Guardar cambios" : "Crear materia"}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

function TopicFormDialog({
  subject,
  editing,
  onClose,
  onSaved,
}: {
  subject: Subject
  editing?: Topic
  onClose: () => void
  onSaved: () => void
}) {
  const [name, setName] = useState(editing?.name ?? "")
  const [status, setStatus] = useState<TopicInput["status"]>(
    editing?.status ?? "Pendiente",
  )
  const [priority, setPriority] = useState<TopicInput["priority"]>(
    editing?.priority ?? "Media",
  )
  const [dueDate, setDueDate] = useState(editing?.dueDate ?? "")
  const [notes, setNotes] = useState(editing?.notes ?? "")
  const [error, setError] = useState("")

  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!name.trim()) {
      setError("Ingresa un nombre para el tema.")
      return
    }
    const input: TopicInput = {
      name: name.trim(),
      status,
      priority,
      dueDate: dueDate || undefined,
      notes,
    }
    if (editing) await catalogGateway.updateTopic(subject.id, editing.id, input)
    else await catalogGateway.createTopic(subject.id, input)
    onSaved()
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog catalog-dialog topic-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="topic-dialog-title"
      >
        <h2 id="topic-dialog-title">
          {editing ? "Editar tema" : "Nuevo tema"}
        </h2>
        <p>Organízalo dentro de {subject.name}.</p>
        <form onSubmit={save}>
          <Field
            id="topic-name"
            label="Nombre del tema"
            placeholder="Ej. Fotosíntesis"
            value={name}
            onChange={(value) => {
              setName(value)
              setError("")
            }}
            autoComplete="off"
            error={error || undefined}
          />
          <div className="topic-form-grid">
            <div className="field-group">
              <label htmlFor="topic-status">Estado</label>
              <div className="input-shell">
                <select
                  id="topic-status"
                  value={status}
                  onChange={(event) =>
                    setStatus(event.target.value as TopicInput["status"])
                  }
                >
                  <option>Pendiente</option>
                  <option>En progreso</option>
                  <option>Completado</option>
                </select>
              </div>
            </div>
            <div className="field-group">
              <label htmlFor="topic-priority">Prioridad</label>
              <div className="input-shell">
                <select
                  id="topic-priority"
                  value={priority}
                  onChange={(event) =>
                    setPriority(event.target.value as TopicInput["priority"])
                  }
                >
                  <option>Baja</option>
                  <option>Media</option>
                  <option>Alta</option>
                </select>
              </div>
            </div>
          </div>
          <div className="field-group">
            <label>
              Fecha límite <span className="optional-label">Opcional</span>
            </label>
            <CalendarPicker
              value={dueDate}
              onChange={setDueDate}
              markedDates={subject.topics
                .map((topic) => topic.dueDate)
                .filter((date): date is string => Boolean(date))}
            />
          </div>
          <div className="field-group">
            <label htmlFor="topic-notes">Notas</label>
            <textarea
              id="topic-notes"
              placeholder="Apuntes, fórmulas o recordatorios..."
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
            />
          </div>
          <div className="dialog-actions">
            <button
              type="button"
              className="secondary-button"
              onClick={onClose}
            >
              Cancelar
            </button>
            <button type="submit" className="submit-button">
              {editing ? "Guardar cambios" : "Crear tema"}
            </button>
          </div>
        </form>
      </section>
    </div>
  )
}

function ConfirmDeleteDialog({
  kind,
  name,
  topicCount = 0,
  scheduledReviews = 0,
  onCancel,
  onConfirm,
}: {
  kind: "subject" | "topic"
  name: string
  topicCount?: number
  scheduledReviews?: number
  onCancel: () => void
  onConfirm: () => void
}) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="delete-title"
      >
        <span className="dialog-icon delete-icon">×</span>
        <h2 id="delete-title">Eliminar “{name}”</h2>
        <p>Esta acción es permanente y no se puede deshacer.</p>
        {kind === "subject" && topicCount > 0 && (
          <div className="delete-warning">
            También se eliminarán {topicCount}{" "}
            {topicCount === 1 ? "tema asociado" : "temas asociados"} y todo su
            historial.
          </div>
        )}
        {kind === "topic" && scheduledReviews > 0 && (
          <div className="delete-warning">
            Se cancelarán {scheduledReviews}{" "}
            {scheduledReviews === 1
              ? "repaso programado"
              : "repasos programados"}
            .
          </div>
        )}
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onCancel}>
            Cancelar
          </button>
          <button type="button" className="danger-button" onClick={onConfirm}>
            Eliminar permanentemente
          </button>
        </div>
      </section>
    </div>
  )
}

function ExportDialog({
  subjects,
  onClose,
}: {
  subjects: Subject[]
  onClose: () => void
}) {
  const [format, setFormat] = useState<"pdf" | "csv" | "json">("pdf")
  const [notice, setNotice] = useState<Notice | null>(null)

  const toast = useToast()

  async function download() {
    if (!subjects.length) {
      toast.show({
        tone: "error",
        message: "No hay materias para exportar.",
      })
      return
    }
    const save = async () => {
      const blob = await catalogGateway.exportCatalog(subjects, format)
      const url = URL.createObjectURL(blob)
      const anchor = document.createElement("a")
      anchor.href = url
      anchor.download = `aslan-materias.${format}`
      anchor.click()
      URL.revokeObjectURL(url)
    }
    await save()
    onClose()
    toast.show({
      tone: "success",
      message: "Exportación lista:",
      highlight: `aslan-materias.${format}`,
      actionLabel: "Descargar de nuevo",
      onAction: save,
      duration: 0,
    })
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog catalog-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="export-title"
      >
        <h2 id="export-title">Exportar organización</h2>
        <p>Descarga tus materias, temas, estados y fechas límite.</p>
        {notice && <NoticeBox notice={notice} />}
        <div className="export-options">
          {(["pdf", "csv", "json"] as const).map((item) => (
            <button
              key={item}
              type="button"
              className={format === item ? "is-selected" : ""}
              onClick={() => setFormat(item)}
            >
              <strong>{item.toUpperCase()}</strong>
              <span>
                {item === "pdf"
                  ? "Documento para compartir"
                  : item === "csv"
                    ? "Hoja de cálculo"
                    : "Respaldo estructurado"}
              </span>
            </button>
          ))}
        </div>
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onClose}>
            Cerrar
          </button>
          <button type="button" className="submit-button" onClick={download}>
            Generar archivo
          </button>
        </div>
      </section>
    </div>
  )
}

function TopicStatusDropdown({
  topic,
  onChange,
}: {
  topic: Topic
  onChange: (status: Topic["status"]) => void
}) {
  const [open, setOpen] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const options: Topic["status"][] = ["Pendiente", "En progreso", "Completado"]
  const statusClass = (status: Topic["status"]) =>
    status.toLowerCase().replace(" ", "-")

  useEffect(() => {
    if (!open) return
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener("pointerdown", closeOnOutsideClick)
    return () =>
      document.removeEventListener("pointerdown", closeOnOutsideClick)
  }, [open])

  return (
    <div className="topic-status-dropdown" ref={containerRef}>
      <button
        type="button"
        className={`topic-status-trigger topic-status-trigger--${statusClass(topic.status)}`}
        onClick={() => setOpen((current) => !current)}
        aria-haspopup="listbox"
        aria-expanded={open}
      >
        <span className="topic-status-dot" />
        <span>{topic.status}</span>
        <svg aria-hidden="true" viewBox="0 0 16 16" fill="none">
          <path
            d="m4 6 4 4 4-4"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </button>
      {open && (
        <div className="topic-status-options" role="listbox">
          {options.map((status) => (
            <button
              type="button"
              role="option"
              aria-selected={topic.status === status}
              className={`topic-status-option topic-status-option--${statusClass(status)}`}
              key={status}
              onClick={() => {
                onChange(status)
                setOpen(false)
              }}
            >
              <span className="topic-status-dot" />
              <span>{status}</span>
              {topic.status === status && <CheckIcon />}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

function SubjectsScreen({
  user,
  subjects,
  selectedId,
  onSelect,
  onRefresh,
  onTopicCompleted,
  onBack,
  onHome,
  onReviews,
  onNotifications,
  onPlan,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
}: {
  user: User
  subjects: Subject[]
  selectedId: string | null
  onSelect: (id: string | null) => void
  onRefresh: () => void
  onTopicCompleted: (subject: Subject, topic: Topic) => void
  onBack: () => void
  onHome: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  const [showArchived, setShowArchived] = useState(false)
  const [subjectEditor, setSubjectEditor] = useState<Subject | "new" | null>(
    null,
  )
  const [topicEditor, setTopicEditor] = useState<Topic | "new" | null>(null)
  const [deleteSubject, setDeleteSubject] = useState<Subject | null>(null)
  const [deleteTopic, setDeleteTopic] = useState<Topic | null>(null)
  const [exportOpen, setExportOpen] = useState(false)
  const [subjectMenuOpen, setSubjectMenuOpen] = useState(false)
  const [topicMenuOpen, setTopicMenuOpen] = useState<string | null>(null)
  const [masteryOpen, setMasteryOpen] = useState(false)
  const selected = subjects.find((subject) => subject.id === selectedId) ?? null
  const compactSubjectTitle = (selected?.topics.length ?? 0) >= 4
  const lowMasteryTopics =
    selected?.topics.filter(
      (topic) =>
        topic.status !== "Completado" &&
        topic.mastery !== undefined &&
        topic.mastery < 50,
    ) ?? []

  useEffect(() => {
    if (!subjectMenuOpen && !topicMenuOpen) return
    const closeMenusOnOutsideClick = (event: PointerEvent) => {
      const target = event.target as Element
      if (
        target.closest(".subject-header-menu") ||
        target.closest(".topic-menu-wrapper")
      ) {
        return
      }
      setSubjectMenuOpen(false)
      setTopicMenuOpen(null)
    }
    document.addEventListener("pointerdown", closeMenusOnOutsideClick)
    return () =>
      document.removeEventListener("pointerdown", closeMenusOnOutsideClick)
  }, [subjectMenuOpen, topicMenuOpen])

  async function refreshed(closeEditors = true) {
    await onRefresh()
    if (closeEditors) {
      setSubjectEditor(null)
      setTopicEditor(null)
    }
  }

  async function changeTopicStatus(topic: Topic, status: Topic["status"]) {
    if (!selected) return
    await catalogGateway.updateTopic(selected.id, topic.id, {
      name: topic.name,
      status,
      priority: topic.priority,
      dueDate: topic.dueDate,
      notes: topic.notes,
    })
    if (topic.status !== "Completado" && status === "Completado")
      onTopicCompleted(selected, topic)
    refreshed(false)
  }

  async function changeTopicDueDate(topic: Topic, dueDate: string) {
    if (!selected) return
    await catalogGateway.updateTopic(selected.id, topic.id, {
      name: topic.name,
      status: topic.status,
      priority: topic.priority,
      dueDate: dueDate || undefined,
      notes: topic.notes,
    })
    refreshed(false)
  }

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="subjects"
        onHome={onHome}
        onSubjects={() => undefined}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="catalog-page">
        {!selected ? (
          <>
            <div className="catalog-heading">
              <div>
                <p className="step-label">Organización</p>
                <h1>Materias</h1>
                <p>
                  Organiza tus temas y detecta rápidamente dónde necesitas
                  enfocarte.
                </p>
              </div>
              <div>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setExportOpen(true)}
                >
                  Exportar
                </button>
                <button
                  type="button"
                  className="submit-button"
                  onClick={() => setSubjectEditor("new")}
                >
                  Nueva materia
                </button>
              </div>
            </div>
            <div className="catalog-tabs">
              <button
                type="button"
                className={!showArchived ? "is-active" : ""}
                onClick={() => setShowArchived(false)}
              >
                Activas
              </button>
              <button
                type="button"
                className={showArchived ? "is-active" : ""}
                onClick={() => setShowArchived(true)}
              >
                Archivadas
              </button>
            </div>
            <div className="subject-grid">
              {subjects
                .filter((subject) => subject.archived === showArchived)
                .map((subject) => {
                  const progress = subjectProgress(subject)
                  const overdue = subject.topics.filter(
                    (topic) => topicTiming(topic) === "overdue",
                  ).length
                  const risk = subject.topics.filter(
                    (topic) => topicTiming(topic) === "risk",
                  ).length
                  return (
                    <article className="subject-card" key={subject.id}>
                      <button
                        type="button"
                        className="subject-card-main"
                        onClick={() => onSelect(subject.id)}
                      >
                        <span
                          className={`subject-glyph subject-color--${subject.color}`}
                        >
                          <SubjectGlyph icon={subject.icon} />
                        </span>
                        <strong>{subject.name}</strong>
                        <small>
                          {subject.topics.length}{" "}
                          {subject.topics.length === 1 ? "tema" : "temas"}
                        </small>
                        <div className="subject-progress">
                          <span>
                            <i style={{ width: `${progress}%` }} />
                          </span>
                          <strong>{progress}%</strong>
                        </div>
                        {(overdue > 0 || risk > 0) && (
                          <p className="subject-alerts">
                            {overdue > 0 && (
                              <span>
                                {overdue} vencido{overdue > 1 ? "s" : ""}
                              </span>
                            )}
                            {risk > 0 && <span>{risk} en riesgo</span>}
                          </p>
                        )}
                      </button>
                      <div className="subject-card-actions">
                        <button
                          type="button"
                          onClick={() => setSubjectEditor(subject)}
                        >
                          Editar
                        </button>
                        <button
                          type="button"
                          onClick={async () => {
                            await catalogGateway.setSubjectArchived(
                              subject.id,
                              !subject.archived,
                            )
                            refreshed(false)
                          }}
                        >
                          {subject.archived ? "Reactivar" : "Archivar"}
                        </button>
                        <button
                          type="button"
                          className="danger-text"
                          onClick={() => setDeleteSubject(subject)}
                        >
                          Eliminar
                        </button>
                      </div>
                    </article>
                  )
                })}
              {!subjects.some(
                (subject) => subject.archived === showArchived,
              ) && (
                <div className="catalog-empty">
                  <SubjectGlyph icon="book" />
                  <h2>
                    {showArchived
                      ? "No hay materias archivadas"
                      : "Aún no tienes materias"}
                  </h2>
                  <p>
                    {showArchived
                      ? "Las materias que archives aparecerán aquí."
                      : "Crea una materia para comenzar a organizar tus temas."}
                  </p>
                </div>
              )}
            </div>
          </>
        ) : (
          <>
            <div className="subject-detail-toolbar">
              <button
                type="button"
                className="subject-back-button"
                onClick={onBack}
                aria-label="Regresar a la última pestaña"
                title="Regresar"
              >
                <img src={arrowBackIcon} alt="" />
              </button>
              <span className="subject-toolbar-section">Organización</span>
              {compactSubjectTitle && <h1>{selected.name}</h1>}
              <div className="subject-header-controls">
                <button
                  type="button"
                  className="icon-action-button mastery-toggle-button"
                  onClick={() => setMasteryOpen(true)}
                  aria-label="Ver dominio de la materia"
                  title="Ver dominio"
                >
                  <img src={boltIcon} alt="" />
                </button>
                <div className="subject-header-menu">
                  <button
                    type="button"
                    className="icon-action-button"
                    onClick={() => setSubjectMenuOpen((current) => !current)}
                    aria-label="Opciones de la materia"
                  >
                    <img src={dotsVertIcon} alt="" />
                  </button>
                  {subjectMenuOpen && (
                    <div className="topic-context-menu subject-context-menu">
                      <button
                        type="button"
                        onClick={() => {
                          setSubjectEditor(selected)
                          setSubjectMenuOpen(false)
                        }}
                      >
                        <img src={editLineIcon} alt="" />
                        Editar
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
            {!compactSubjectTitle && (
              <div className="subject-expanded-title">
                <div
                  className={`subject-glyph subject-glyph--large subject-color--${selected.color}`}
                >
                  <SubjectGlyph icon={selected.icon} />
                </div>
                <div>
                  <p className="step-label">Materia</p>
                  <h1>{selected.name}</h1>
                  <p>
                    {subjectProgress(selected)}% completado ·{" "}
                    {selected.topics.length} temas
                  </p>
                </div>
              </div>
            )}
            <div className="topics-card topics-list">
              {selected.topics.map((topic) => {
                const timing = topicTiming(topic)
                const due = topicDuePresentation(topic)
                return (
                  <div
                    className={`topic-row ${
                      timing ? `topic-row--${timing}` : ""
                    }`}
                    key={topic.id}
                  >
                    <span
                      className={`topic-priority-indicator topic-priority-indicator--${topic.priority.toLowerCase()}`}
                      title={`Prioridad ${topic.priority.toLowerCase()}`}
                    >
                      <img src={circleIcon} alt="" />
                    </span>
                    <div className="topic-primary">
                      <div className="topic-name-line">
                        <strong>{topic.name}</strong>
                      </div>
                      <div className="topic-meta-line">
                        <span className={`topic-due topic-due--${due.tone}`}>
                          <span className="topic-due-icon" aria-hidden="true" />
                          {due.label}
                        </span>
                        <TopicStatusDropdown
                          topic={topic}
                          onChange={(status) =>
                            changeTopicStatus(topic, status)
                          }
                        />
                      </div>
                    </div>
                    <div className="topic-actions">
                      <button
                        type="button"
                        onClick={() => setTopicEditor(topic)}
                        aria-label={`Editar ${topic.name}`}
                        title="Editar"
                      >
                        <img src={editLineIcon} alt="" />
                      </button>
                      <CalendarPicker
                        iconOnly
                        value={topic.dueDate ?? ""}
                        onChange={(date) => changeTopicDueDate(topic, date)}
                        markedDates={selected.topics
                          .map((item) => item.dueDate)
                          .filter((date): date is string => Boolean(date))}
                      />
                      <div className="topic-menu-wrapper">
                        <button
                          type="button"
                          onClick={() =>
                            setTopicMenuOpen((current) =>
                              current === topic.id ? null : topic.id,
                            )
                          }
                          aria-label={`Más opciones para ${topic.name}`}
                          title="Más opciones"
                        >
                          <img src={dotsVertIcon} alt="" />
                        </button>
                        {topicMenuOpen === topic.id && (
                          <div className="topic-context-menu">
                            <button
                              type="button"
                              onClick={() => {
                                setTopicEditor(topic)
                                setTopicMenuOpen(null)
                              }}
                            >
                              <img src={editLineIcon} alt="" />
                              Editar
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setTopicEditor(topic)
                                setTopicMenuOpen(null)
                              }}
                            >
                              <img src={calendarIcon} alt="" />
                              Modificar fecha límite
                            </button>
                            <button
                              type="button"
                              className="danger-text"
                              onClick={() => {
                                setDeleteTopic(topic)
                                setTopicMenuOpen(null)
                              }}
                            >
                              <img src={deleteIcon} alt="" />
                              Eliminar
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                )
              })}
              <button
                type="button"
                className="add-topic-button"
                onClick={() => setTopicEditor("new")}
              >
                <img src={addIcon} alt="" />
                Agregar Tema
              </button>
            </div>
          </>
        )}
      </section>
      {masteryOpen && selected && (
        <div
          className="mastery-drawer-backdrop"
          role="presentation"
          onClick={() => setMasteryOpen(false)}
        >
          <aside
            className="mastery-drawer"
            role="dialog"
            aria-modal="true"
            aria-labelledby="mastery-drawer-title"
            onClick={(event) => event.stopPropagation()}
          >
            <header>
              <div>
                <h2 id="mastery-drawer-title">Dominio de {selected.name}</h2>
              </div>
              <button
                type="button"
                onClick={() => setMasteryOpen(false)}
                aria-label="Cerrar panel de dominio"
              >
                ×
              </button>
            </header>
            <div className="mastery-drawer-content">
              <section className="mastery-insight-card">
                <h3>En riesgo</h3>
                <div className="mastery-insight-visual">
                  {lowMasteryTopics.length ? <SparkIcon /> : <CheckIcon />}
                </div>
                <strong>
                  {lowMasteryTopics.length
                    ? `${lowMasteryTopics.length} ${
                        lowMasteryTopics.length === 1
                          ? "tema necesita"
                          : "temas necesitan"
                      } refuerzo`
                    : "Todos los temas van por buen camino"}
                </strong>
              </section>
              <section className="mastery-summary-card">
                <h3>Progreso</h3>
                <div className="mastery-progress-value">
                  <span>→</span>
                  <strong>{subjectProgress(selected)}%</strong>
                </div>
                <div className="mastery-progress-track">
                  <span style={{ width: `${subjectProgress(selected)}%` }} />
                </div>
                <div className="mastery-progress-legend">
                  <span>
                    <CheckIcon />
                    {
                      selected.topics.filter(
                        (topic) => topic.status === "Completado",
                      ).length
                    }{" "}
                    completados
                  </span>
                  <span>
                    <i />
                    {
                      selected.topics.filter(
                        (topic) => topic.status !== "Completado",
                      ).length
                    }{" "}
                    activos
                  </span>
                </div>
              </section>
              <section className="mastery-summary-card">
                <h3>Dominio por tema</h3>
                <div className="mastery-topic-list">
                  {selected.topics.map((topic) => (
                    <div key={topic.id}>
                      <span>{topic.name}</span>
                      <i>
                        <b style={{ width: `${topic.mastery ?? 0}%` }} />
                      </i>
                      <strong>
                        {topic.mastery === undefined
                          ? "Sin datos"
                          : `${topic.mastery}%`}
                      </strong>
                    </div>
                  ))}
                  {!selected.topics.length && (
                    <p>No hay temas para analizar todavía.</p>
                  )}
                </div>
              </section>
              <section className="mastery-summary-card mastery-completed-card">
                <h3>Completados</h3>
                <div className="mastery-progress-value">
                  <span>→</span>
                  <strong>
                    Esta semana:{" "}
                    {
                      selected.topics.filter(
                        (topic) => topic.status === "Completado",
                      ).length
                    }
                  </strong>
                </div>
                <div className="mastery-week-chart">
                  {["L", "M", "X", "J", "V", "S", "D"].map((day, index) => (
                    <div key={day}>
                      <span
                        className={
                          index <
                          selected.topics.filter(
                            (topic) => topic.status === "Completado",
                          ).length
                            ? "is-complete"
                            : ""
                        }
                      />
                      <small>{day}</small>
                    </div>
                  ))}
                </div>
              </section>
            </div>
          </aside>
        </div>
      )}
      {subjectEditor && (
        <SubjectFormDialog
          subjects={subjects}
          editing={subjectEditor === "new" ? undefined : subjectEditor}
          onClose={() => setSubjectEditor(null)}
          onSaved={() => refreshed()}
        />
      )}
      {topicEditor && selected && (
        <TopicFormDialog
          subject={selected}
          editing={topicEditor === "new" ? undefined : topicEditor}
          onClose={() => setTopicEditor(null)}
          onSaved={() => refreshed()}
        />
      )}
      {deleteSubject && (
        <ConfirmDeleteDialog
          kind="subject"
          name={deleteSubject.name}
          topicCount={deleteSubject.topics.length}
          onCancel={() => setDeleteSubject(null)}
          onConfirm={async () => {
            await catalogGateway.deleteSubject(deleteSubject.id)
            setDeleteSubject(null)
            onSelect(null)
            refreshed(false)
          }}
        />
      )}
      {deleteTopic && selected && (
        <ConfirmDeleteDialog
          kind="topic"
          name={deleteTopic.name}
          scheduledReviews={deleteTopic.scheduledReviews}
          onCancel={() => setDeleteTopic(null)}
          onConfirm={async () => {
            await catalogGateway.deleteTopic(selected.id, deleteTopic.id)
            setDeleteTopic(null)
            refreshed(false)
          }}
        />
      )}
      {exportOpen && (
        <ExportDialog
          subjects={subjects}
          onClose={() => setExportOpen(false)}
        />
      )}
    </main>
  )
}

function RatingDialog({
  review,
  onClose,
  onRate,
  onUnrated,
}: {
  review: Review
  onClose: () => void
  onRate: (rating: number) => void
  onUnrated: () => void
}) {
  const [rating, setRating] = useState(0)
  const labels = ["Muy difícil", "Difícil", "Regular", "Fácil", "Muy fácil"]
  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog rating-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="rating-title"
      >
        <p className="step-label">Repaso completado</p>
        <h2 id="rating-title">¿Qué tan fácil fue recordar?</h2>
        <p>
          Tu respuesta ajustará el factor de facilidad y el próximo intervalo de
          “{review.topicName}”.
        </p>
        <div className="rating-options">
          {[1, 2, 3, 4, 5].map((value) => (
            <button
              key={value}
              type="button"
              className={rating === value ? "is-selected" : ""}
              onClick={() => setRating(value)}
            >
              <strong>{value}</strong>
              <span>{labels[value - 1]}</span>
            </button>
          ))}
        </div>
        <div className="rating-hint">
          {rating > 0 && rating <= 2
            ? "Una calificación baja acortará el próximo intervalo."
            : rating >= 4
              ? "El próximo repaso se programará con un intervalo más amplio."
              : "Selecciona una calificación entre 1 y 5."}
        </div>
        <div className="dialog-actions">
          <button
            type="button"
            className="text-danger-button"
            onClick={onUnrated}
          >
            Cerrar sin calificar
          </button>
          <button type="button" className="secondary-button" onClick={onClose}>
            Seguir repasando
          </button>
          <button
            type="button"
            className="submit-button"
            disabled={!rating}
            onClick={() => onRate(rating)}
          >
            Guardar calificación
          </button>
        </div>
      </section>
    </div>
  )
}

function ReviewsScreen({
  user,
  subjects,
  reviews,
  notificationsEnabled,
  onNotificationsChange,
  focusedReviewId,
  onRefresh,
  onHome,
  onSubjects,
  onContextNotifications,
  onPlan,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
  onNotify,
}: {
  user: User
  subjects: Subject[]
  reviews: Review[]
  notificationsEnabled: boolean
  onNotificationsChange: (enabled: boolean) => void
  focusedReviewId: string | null
  onRefresh: () => void
  onHome: () => void
  onSubjects: () => void
  onContextNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
  onNotify: (title: string, detail: string, reviewId?: string) => void
}) {
  const [tab, setTab] = useState<"upcoming" | "history">("upcoming")
  const [activeReview, setActiveReview] = useState<Review | null>(null)
  const [selectedTopicId, setSelectedTopicId] = useState(
    reviews.find((review) => review.status === "Completado")?.topicId ??
      subjects.flatMap((subject) => subject.topics)[0]?.id ??
      "",
  )
  const today = new Date().toISOString().slice(0, 10)
  const upcoming = reviews.filter((review) => review.status !== "Completado")
  const history = reviews.filter(
    (review) =>
      review.topicId === selectedTopicId && review.status === "Completado",
  )
  const overdueHistory = reviews.filter(
    (review) =>
      review.topicId === selectedTopicId &&
      review.status !== "Completado" &&
      review.dueDate < today,
  )
  const topicOptions = Array.from(
    new Map(
      subjects
        .flatMap((subject) =>
          subject.topics.map((topic) => [
            topic.id,
            { ...topic, subjectName: subject.name },
          ]),
        )
        .concat(
          reviews.map((review) => [
            review.topicId,
            {
              id: review.topicId,
              name: review.topicName,
              subjectName: review.subjectName,
            },
          ]),
        ),
    ).values(),
  )
  const average = history.length
    ? Math.round(
        (history.reduce((sum, review) => sum + (review.rating ?? 0), 0) /
          history.length) *
          20,
      )
    : null

  useEffect(() => {
    if (!focusedReviewId) return
    const review = reviews.find((item) => item.id === focusedReviewId)
    if (review && review.status !== "Completado") setActiveReview(review)
  }, [focusedReviewId, reviews])

  async function rate(review: Review, rating: number) {
    const result = await reviewGateway.rate(review.id, rating)
    setActiveReview(null)
    onRefresh()
    if (notificationsEnabled) {
      onNotify(
        "Nuevo repaso programado",
        `${result.next.topicName} · ${new Date(`${result.next.dueDate}T12:00:00`).toLocaleDateString("es", { day: "numeric", month: "long" })}`,
        result.next.id,
      )
    }
  }

  async function leaveUnrated(review: Review) {
    await reviewGateway.leaveUnrated(review.id)
    setActiveReview(null)
    onRefresh()
    if (notificationsEnabled)
      onNotify(
        "Repaso pendiente de calificar",
        `La calificación de ${review.topicName} quedó pendiente.`,
        review.id,
      )
  }

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="reviews"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={() => undefined}
        onNotifications={onContextNotifications}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="reviews-page">
        <div className="catalog-heading reviews-heading">
          <div>
            <p className="step-label">Repetición espaciada · SM-2</p>
            <h1>Repasos</h1>
            <p>
              Refuerza cada tema en el momento óptimo según tu retención real.
            </p>
          </div>
          <div className="review-notification-setting">
            <span>Notificaciones</span>
            <button
              type="button"
              role="switch"
              aria-checked={notificationsEnabled}
              className={`switch ${notificationsEnabled ? "switch--on" : ""}`}
              onClick={() => onNotificationsChange(!notificationsEnabled)}
            >
              <span />
            </button>
          </div>
        </div>
        {!notificationsEnabled && (
          <div className="privacy-state review-privacy">
            Las notificaciones están desactivadas. Los repasos seguirán
            programándose y aparecerán aquí y en el plan semanal.
          </div>
        )}
        <div className="catalog-tabs">
          <button
            type="button"
            className={tab === "upcoming" ? "is-active" : ""}
            onClick={() => setTab("upcoming")}
          >
            Próximos
          </button>
          <button
            type="button"
            className={tab === "history" ? "is-active" : ""}
            onClick={() => setTab("history")}
          >
            Historial y retención
          </button>
        </div>
        {tab === "upcoming" ? (
          <div className="reviews-layout">
            <div className="review-list">
              {upcoming
                .sort((a, b) => a.dueDate.localeCompare(b.dueDate))
                .map((review) => {
                  const overdue = review.dueDate < today
                  const soon =
                    !overdue &&
                    (new Date(review.dueDate).getTime() - Date.now()) /
                      86_400_000 <=
                      2
                  return (
                    <article
                      className={`review-card ${
                        overdue ? "review-card--overdue" : ""
                      }`}
                      key={review.id}
                    >
                      <div className="review-date">
                        <strong>
                          {new Date(`${review.dueDate}T12:00:00`).getDate()}
                        </strong>
                        <span>
                          {new Date(
                            `${review.dueDate}T12:00:00`,
                          ).toLocaleDateString("es", { month: "short" })}
                        </span>
                      </div>
                      <div className="review-copy">
                        <span>{review.subjectName}</span>
                        <h2>{review.topicName}</h2>
                        <p>
                          Intervalo actual: {review.intervalDays}{" "}
                          {review.intervalDays === 1 ? "día" : "días"} · Factor{" "}
                          {review.easeFactor.toFixed(2)}
                        </p>
                      </div>
                      <span
                        className={`review-state ${
                          overdue ? "is-overdue" : soon ? "is-soon" : ""
                        }`}
                      >
                        {review.status === "Pendiente de calificar"
                          ? "Sin calificar"
                          : overdue
                            ? "Vencido"
                            : soon
                              ? "Vence pronto"
                              : "Programado"}
                      </span>
                      <button
                        type="button"
                        className="submit-button"
                        onClick={() => setActiveReview(review)}
                      >
                        {review.status === "Pendiente de calificar"
                          ? "Calificar"
                          : "Iniciar repaso"}
                      </button>
                    </article>
                  )
                })}
              {!upcoming.length && (
                <div className="catalog-empty">
                  <SparkIcon />
                  <h2>No tienes repasos pendientes</h2>
                  <p>
                    Cuando completes un tema, Aslan programará el primero
                    automáticamente.
                  </p>
                </div>
              )}
            </div>
            <aside className="sm2-card">
              <span>Cómo funciona</span>
              <h2>Intervalos que se adaptan</h2>
              <p>
                Después de cada repaso, califica qué tan fácil fue recordar.
                Aslan ajustará el factor de facilidad y calculará la siguiente
                fecha.
              </p>
              <div>
                <strong>1–2</strong>
                <span>Intervalo más corto</span>
              </div>
              <div>
                <strong>3</strong>
                <span>Se mantiene estable</span>
              </div>
              <div>
                <strong>4–5</strong>
                <span>Intervalo más amplio</span>
              </div>
            </aside>
          </div>
        ) : (
          <div className="history-layout">
            <aside className="history-topics">
              <span>Temas</span>
              {topicOptions.map((topic) => (
                <button
                  key={topic.id}
                  type="button"
                  className={selectedTopicId === topic.id ? "is-active" : ""}
                  onClick={() => setSelectedTopicId(topic.id)}
                >
                  <strong>{topic.name}</strong>
                  <small>{topic.subjectName}</small>
                </button>
              ))}
            </aside>
            <section className="retention-panel">
              {history.length || overdueHistory.length ? (
                <>
                  <div className="retention-heading">
                    <div>
                      <span>Retención estimada</span>
                      <strong>
                        {average === null ? "Sin datos" : `${average}%`}
                      </strong>
                    </div>
                    <p>
                      {average === null
                        ? "El repaso venció sin una calificación registrada."
                        : average < 50
                          ? "Este tema requiere refuerzo."
                          : "La retención muestra una tendencia estable."}
                    </p>
                  </div>
                  <div className="retention-chart">
                    {history.map((review, index) => (
                      <div key={review.id}>
                        <span
                          style={{ height: `${(review.rating ?? 0) * 18}%` }}
                        />
                        <small>R{index + 1}</small>
                      </div>
                    ))}
                  </div>
                  <div className="history-table">
                    <div>
                      <span>Fecha</span>
                      <span>Calificación</span>
                      <span>Intervalo</span>
                      <span>Factor</span>
                    </div>
                    {history.map((review) => (
                      <div key={review.id}>
                        <span>{review.completedAt}</span>
                        <strong>{review.rating}/5</strong>
                        <span>{review.intervalDays} días</span>
                        <span>{review.easeFactor.toFixed(2)}</span>
                      </div>
                    ))}
                    {overdueHistory.map((review) => (
                      <div className="history-overdue-row" key={review.id}>
                        <span>{review.dueDate}</span>
                        <strong>Vencido</strong>
                        <span>Sin calificar</span>
                        <span>{review.easeFactor.toFixed(2)}</span>
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <div className="history-empty">
                  <SparkIcon />
                  <h2>No hay repasos registrados todavía</h2>
                  <p>
                    El historial y las estadísticas aparecerán después de
                    calificar el primer repaso de este tema.
                  </p>
                </div>
              )}
            </section>
          </div>
        )}
      </section>
      {activeReview && (
        <RatingDialog
          review={activeReview}
          onClose={() => setActiveReview(null)}
          onRate={(rating) => rate(activeReview, rating)}
          onUnrated={() => leaveUnrated(activeReview)}
        />
      )}
    </main>
  )
}

function parseTimeInput(value: string) {
  const normalized = value
    .trim()
    .toLowerCase()
    .replace(/\./g, "")
    .replace(/\s+/g, "")
  const match = normalized.match(/^(\d{1,2})(?::?(\d{2}))?(am|pm|a|p)?$/)
  if (!match) return null
  let hour = Number(match[1])
  const minute = Number(match[2] ?? "0")
  const meridiem = match[3]
  if (minute > 59) return null
  if (meridiem) {
    if (hour < 1 || hour > 12) return null
    if (meridiem.startsWith("p") && hour !== 12) hour += 12
    if (meridiem.startsWith("a") && hour === 12) hour = 0
  } else if (hour > 23) {
    return null
  }
  return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`
}

function formatTime12(time: string) {
  const [hour, minute] = time.split(":").map(Number)
  const suffix = hour >= 12 ? "p. m." : "a. m."
  const displayHour = hour % 12 || 12
  return `${displayHour}:${String(minute).padStart(2, "0")} ${suffix}`
}

function TimeCombobox({
  value,
  onChange,
  onValidityChange,
}: {
  value: string
  onChange: (value: string) => void
  onValidityChange: (valid: boolean) => void
}) {
  const [open, setOpen] = useState(false)
  const [searching, setSearching] = useState(false)
  const containerRef = useRef<HTMLDivElement>(null)
  const selectedOptionRef = useRef<HTMLButtonElement>(null)
  const options = Array.from({ length: 96 }, (_, index) => {
    const hour = Math.floor(index / 4)
    const minute = (index % 4) * 15
    return `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`
  })
  const parsed = parseTimeInput(value)
  const compactQuery = value.toLowerCase().replace(/\s|\./g, "")
  const filteredOptions = (() => {
    if (!searching || !value.trim()) return options
    if (parsed) {
      const hasExplicitMinutes =
        value.includes(":") || value.replace(/\D/g, "").length > 2
      if (hasExplicitMinutes) {
        const exact = options.filter((option) => option === parsed)
        return exact.length
          ? exact
          : options.filter((option) => option.startsWith(parsed.slice(0, 3)))
      }
      return options.filter((option) => option.startsWith(parsed.slice(0, 3)))
    }
    return options.filter(
      (option) =>
        option.includes(compactQuery) ||
        formatTime12(option)
          .toLowerCase()
          .replace(/\s|\./g, "")
          .includes(compactQuery),
    )
  })()

  useEffect(() => {
    onValidityChange(Boolean(parsed))
  }, [onValidityChange, parsed])

  useEffect(() => {
    if (!open) return
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (!containerRef.current?.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener("pointerdown", closeOnOutsideClick)
    return () =>
      document.removeEventListener("pointerdown", closeOnOutsideClick)
  }, [open])

  useEffect(() => {
    if (!open || searching) return
    requestAnimationFrame(() =>
      selectedOptionRef.current?.scrollIntoView({ block: "nearest" }),
    )
  }, [open, searching])

  return (
    <div className="time-combobox" ref={containerRef}>
      <div
        className={`time-input-shell ${value && !parsed ? "is-invalid" : ""}`}
      >
        <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
          <circle
            cx="12"
            cy="12"
            r="8"
            stroke="currentColor"
            strokeWidth="1.6"
          />
          <path
            d="M12 8v4l3 2"
            stroke="currentColor"
            strokeWidth="1.6"
            strokeLinecap="round"
          />
        </svg>
        <TextControl
          type="text"
          value={value}
          placeholder="Ej. 19:30 o 7:30 pm"
          autoComplete="off"
          onFocus={() => {
            setSearching(false)
            setOpen(true)
          }}
          onChange={(event) => {
            onChange(event.target.value)
            setSearching(true)
            setOpen(true)
          }}
          aria-label="Nueva hora"
          aria-expanded={open}
          aria-autocomplete="list"
        />
      </div>
      {open && (
        <div className="time-options" role="listbox">
          {filteredOptions.map((option) => (
            <ControlButton
              type="button"
              role="option"
              aria-selected={parsed === option}
              ref={parsed === option ? selectedOptionRef : undefined}
              key={option}
              onClick={() => {
                onChange(option)
                setSearching(false)
                setOpen(false)
              }}
            >
              <strong>{option}</strong>
              <span>{formatTime12(option)}</span>
              {parsed === option && <CheckIcon />}
            </ControlButton>
          ))}
          {!filteredOptions.length && <p>No encontramos una hora válida.</p>}
        </div>
      )}
    </div>
  )
}

function ReprogramDialog({
  session,
  sessions,
  onClose,
  onSave,
}: {
  session: PlanSession
  sessions: PlanSession[]
  onClose: () => void
  onSave: (date: string, time: string) => void
}) {
  const [date, setDate] = useState(session.date)
  const [time, setTime] = useState(session.time)
  const [timeValid, setTimeValid] = useState(true)
  const [error, setError] = useState("")

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const normalizedTime = parseTimeInput(time)
    if (!date || !normalizedTime || !timeValid) {
      setError(
        !date
          ? "Selecciona una fecha para la sesión."
          : "Ingresa una hora válida en formato de 12 o 24 horas.",
      )
      return
    }
    const conflict = sessions.some(
      (item) =>
        item.id !== session.id &&
        item.date === date &&
        item.time === normalizedTime,
    )
    if (conflict) {
      setError("Este horario ya está ocupado por otra sesión del plan.")
      return
    }
    onSave(date, normalizedTime)
  }

  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog catalog-dialog reprogram-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="reprogram-title"
      >
        <h2 id="reprogram-title">Reprogramar sesión</h2>
        <p>
          {session.subjectName} · {session.topicName}
        </p>
        <form onSubmit={submit}>
          {error && (
            <NoticeBox
              notice={{
                type: "error",
                title: "Conflicto de horario",
                detail: error,
              }}
            />
          )}
          <div className="reprogram-fields">
            <div className="field-group">
              <label>Nueva fecha</label>
              <CalendarPicker
                inline
                value={date}
                markedDates={sessions.map((item) => item.date)}
                onChange={(nextDate) => {
                  setDate(nextDate)
                  setError("")
                }}
              />
            </div>
            <div className="field-group">
              <label>Nueva hora</label>
              <TimeCombobox
                value={time}
                onValidityChange={setTimeValid}
                onChange={(nextTime) => {
                  setTime(nextTime)
                  setError("")
                }}
              />
              <span className="field-hint">
                Puedes escribirla en formato de 12 o 24 horas.
              </span>
            </div>
          </div>
          <div className="dialog-actions">
            <ControlButton
              type="button"
              className="secondary-button"
              onClick={onClose}
            >
              Cancelar
            </ControlButton>
            <ControlButton type="submit" className="submit-button">
              Guardar horario
            </ControlButton>
          </div>
        </form>
      </section>
    </div>
  )
}

function DeletePlanDialog({
  sessionCount,
  onCancel,
  onConfirm,
}: {
  sessionCount: number
  onCancel: () => void
  onConfirm: () => void
}) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="delete-plan-title"
      >
        <span className="dialog-icon delete-icon">×</span>
        <h2 id="delete-plan-title">Eliminar el plan semanal</h2>
        <p>
          Se eliminarán las {sessionCount} sesiones de esta semana. Tus materias
          y temas no se verán afectados.
        </p>
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onCancel}>
            Cancelar
          </button>
          <button type="button" className="danger-button" onClick={onConfirm}>
            Eliminar todo el plan
          </button>
        </div>
      </section>
    </div>
  )
}

function WeeklyPlanScreen({
  user,
  subjects,
  reviews,
  settings,
  plan,
  onPlanChange,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
}: {
  user: User
  subjects: Subject[]
  reviews: Review[]
  settings: StudySettings
  plan: WeeklyPlan | null
  onPlanChange: (plan: WeeklyPlan | null) => void
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  const [notice, setNotice] = useState<Notice | null>(null)
  const [reprogramming, setReprogramming] = useState<PlanSession | null>(null)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [closedCompliance, setClosedCompliance] = useState<number | null>(null)
  const weekDays = [
    "Lunes",
    "Martes",
    "Miércoles",
    "Jueves",
    "Viernes",
    "Sábado",
    "Domingo",
  ]

  async function generate() {
    setGenerating(true)
    const generated = await plannerGateway.generate(
      subjects,
      settings.pomodoroMinutes,
      reviews,
    )
    setGenerating(false)
    if (!generated) {
      setNotice({
        type: "error",
        title: "Primero registra materias y temas",
        detail:
          "Necesitas al menos un tema pendiente o en progreso para generar tu plan.",
      })
      return
    }
    onPlanChange(generated)
    setNotice({
      type: "success",
      title: "Plan semanal generado",
      detail:
        "Distribuimos las sesiones según prioridad, carga académica y tus horarios productivos.",
    })
  }

  async function updateSessions(sessions: PlanSession[]) {
    if (!plan) return
    const saved = await plannerGateway.save({ ...plan, sessions })
    onPlanChange(saved)
  }

  function updateSession(id: string, changes: Partial<PlanSession>) {
    if (!plan) return
    updateSessions(
      plan.sessions.map((session) =>
        session.id === id ? { ...session, ...changes } : session,
      ),
    )
  }

  async function exportPlan(format: "pdf" | "csv") {
    if (!plan || !plan.sessions.length) {
      setNotice({
        type: "error",
        title: "No hay un plan disponible",
        detail: "Genera el plan de la semana antes de exportarlo.",
      })
      return
    }
    const blob = await plannerGateway.exportPlan(plan, format)
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement("a")
    anchor.href = url
    anchor.download = `aslan-plan-semanal.${format}`
    anchor.click()
    URL.revokeObjectURL(url)
    setNotice({
      type: "success",
      title: "Exportación lista",
      detail: `El plan se descargó en formato ${format.toUpperCase()}.`,
    })
  }

  const completed =
    plan?.sessions.filter((session) => session.status === "Realizada").length ??
    0
  const compliance = plan?.sessions.length
    ? Math.round((completed / plan.sessions.length) * 100)
    : 0
  const weekStart = plan ? new Date(`${plan.weekStart}T12:00:00`) : null

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="plan"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={() => undefined}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="plan-page">
        <div className="catalog-heading plan-heading">
          <div>
            <p className="step-label">Planificación inteligente</p>
            <h1>Plan semanal</h1>
            <p>
              {plan
                ? `Semana del ${weekStart?.toLocaleDateString("es", { day: "numeric", month: "long" })}`
                : "Organiza automáticamente tus sesiones de la semana."}
            </p>
          </div>
          <div>
            {plan && (
              <>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => exportPlan("csv")}
                >
                  CSV
                </button>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => exportPlan("pdf")}
                >
                  PDF
                </button>
              </>
            )}
            {plan && (
              <button
                type="button"
                className="secondary-button danger-text"
                onClick={() => setDeleteOpen(true)}
              >
                Eliminar plan
              </button>
            )}
            {plan && (
              <button
                type="button"
                className="submit-button"
                onClick={generate}
              >
                {generating ? "Generando..." : "Generar de nuevo"}
              </button>
            )}
          </div>
        </div>
        {notice && <NoticeBox notice={notice} />}
        {!plan ? (
          <div className="plan-empty">
            <SparkIcon />
            <h2>Tu semana está lista para organizarse</h2>
            <p>
              Aslan priorizará fechas límite, carga académica, repasos
              pendientes y tus horarios más productivos.
            </p>
            <button type="button" className="submit-button" onClick={generate}>
              Generar mi plan semanal
            </button>
            <small>También se generará automáticamente cada lunes.</small>
          </div>
        ) : (
          <>
            <div className="plan-summary">
              <div>
                <span>Cumplimiento semanal</span>
                <strong>{compliance}%</strong>
                <div className="compliance-track">
                  <i style={{ width: `${compliance}%` }} />
                </div>
                <small>
                  {completed} de {plan.sessions.length} sesiones realizadas
                </small>
              </div>
              <div>
                <span>Sesiones</span>
                <strong>{plan.sessions.length}</strong>
                <small>Planificadas esta semana</small>
              </div>
              <div>
                <span>Repasos incluidos</span>
                <strong>
                  {
                    plan.sessions.filter((session) => session.type === "Repaso")
                      .length
                  }
                </strong>
                <small>Integrados automáticamente</small>
              </div>
              <div>
                <span>Estado</span>
                <strong>
                  {plan.sessions.every(
                    (session) => session.status !== "Propuesta",
                  )
                    ? "Confirmado"
                    : "Por revisar"}
                </strong>
                <small>Confirma las propuestas</small>
              </div>
            </div>
            <div className="week-board">
              {weekDays.map((day, index) => {
                if (!weekStart) return null
                const date = new Date(weekStart)
                date.setDate(date.getDate() + index)
                const key = date.toISOString().slice(0, 10)
                const daySessions = plan.sessions
                  .filter((session) => session.date === key)
                  .sort((a, b) => a.time.localeCompare(b.time))
                return (
                  <section className="plan-day" key={day}>
                    <header>
                      <div>
                        <strong>{day}</strong>
                        <span>
                          {date.toLocaleDateString("es", {
                            day: "numeric",
                            month: "short",
                          })}
                        </span>
                      </div>
                      <small>
                        {daySessions.length}{" "}
                        {daySessions.length === 1 ? "sesión" : "sesiones"}
                      </small>
                    </header>
                    <div className="day-sessions">
                      {daySessions.map((session) => (
                        <article
                          className={`plan-session plan-session--${session.status.toLowerCase()}`}
                          key={session.id}
                        >
                          <time>{session.time}</time>
                          <div className="plan-session-copy">
                            <span>
                              {session.subjectName} · {session.type}
                            </span>
                            <strong>{session.topicName}</strong>
                            <small>{session.duration} minutos</small>
                          </div>
                          <span className="plan-status">{session.status}</span>
                          <div className="plan-session-actions">
                            {session.status === "Propuesta" && (
                              <button
                                type="button"
                                onClick={() =>
                                  updateSession(session.id, {
                                    status: "Programada",
                                  })
                                }
                              >
                                Confirmar
                              </button>
                            )}
                            {session.status !== "Realizada" && (
                              <button
                                type="button"
                                onClick={() => setReprogramming(session)}
                              >
                                Reprogramar
                              </button>
                            )}
                            {session.status === "Programada" && (
                              <button
                                type="button"
                                onClick={() =>
                                  updateSession(session.id, {
                                    status: "Realizada",
                                  })
                                }
                              >
                                Marcar realizada
                              </button>
                            )}
                            {session.status === "Realizada" && (
                              <button
                                type="button"
                                onClick={() =>
                                  updateSession(session.id, {
                                    status: "Programada",
                                  })
                                }
                              >
                                Desmarcar
                              </button>
                            )}
                            <button
                              type="button"
                              className="danger-text"
                              onClick={() =>
                                updateSessions(
                                  plan.sessions.filter(
                                    (item) => item.id !== session.id,
                                  ),
                                )
                              }
                            >
                              Eliminar
                            </button>
                          </div>
                        </article>
                      ))}
                      {!daySessions.length && (
                        <p className="day-empty">Sin sesiones</p>
                      )}
                    </div>
                  </section>
                )
              })}
            </div>
            <section className="week-analysis">
              <div>
                <span>Análisis de cumplimiento</span>
                <h2>
                  {closedCompliance === null
                    ? "Cierre semanal pendiente"
                    : `${closedCompliance}% registrado`}
                </h2>
                <p>
                  {closedCompliance === null
                    ? "Al terminar la semana, Aslan comparará las sesiones planificadas con las realizadas y guardará el resultado en tu historial."
                    : closedCompliance === 100
                      ? "Completaste todas las sesiones planificadas esta semana."
                      : closedCompliance === 0
                        ? "No se registraron sesiones realizadas. El resultado quedó guardado para ajustar la próxima semana."
                        : "El resultado quedó guardado y se usará para ajustar la carga del próximo plan."}
                </p>
              </div>
              <button
                type="button"
                className="secondary-button"
                onClick={() => setClosedCompliance(compliance)}
              >
                Calcular cierre semanal
              </button>
            </section>
          </>
        )}
      </section>
      {reprogramming && plan && (
        <ReprogramDialog
          session={reprogramming}
          sessions={plan.sessions}
          onClose={() => setReprogramming(null)}
          onSave={(date, time) => {
            updateSession(reprogramming.id, { date, time })
            setReprogramming(null)
          }}
        />
      )}
      {deleteOpen && plan && (
        <DeletePlanDialog
          sessionCount={plan.sessions.length}
          onCancel={() => setDeleteOpen(false)}
          onConfirm={() => {
            onPlanChange(null)
            setDeleteOpen(false)
          }}
        />
      )}
    </main>
  )
}

function StudySetupScreen({
  user,
  settings,
  subjects,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onSettings,
  onProfile,
  onLogout,
  onStart,
}: {
  user: User
  settings: StudySettings
  subjects: Subject[]
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
  onStart: (session: StudySession) => void
}) {
  const [subject, setSubject] = useState("")
  const [topic, setTopic] = useState("")
  const [sound, setSound] = useState("")
  const [suggestion, setSuggestion] = useState<StudySuggestion | null>(null)
  const [submitted, setSubmitted] = useState(false)
  const selectedSubject = subjects.find(
  (item) => !item.archived && item.name === subject,
)
const availableTopics = selectedSubject?.topics ?? []

  useEffect(() => {
    if (!subject || !topic.trim()) {
      setSuggestion(null)
      return
    }
    let active = true
    studyGateway.getSuggestion(user.id, subject, topic).then((result) => {
      if (!active) return
      setSuggestion(result)
      setSound(result.sound)
    })
    return () => {
      active = false
    }
  }, [subject, topic, user.id])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitted(true)
    if (!subject || !topic.trim()) return
    const selectedSuggestion =
      suggestion ?? (await studyGateway.getSuggestion(user.id, subject, topic))
    const session = await studyGateway.startSession({
      subject,
      topic: topic.trim(),
      sound: sound || selectedSuggestion.sound,
      technique: selectedSuggestion.technique,
      plannedCycles: 4,
    })
    onStart(session)
  }

return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="study"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={() => undefined}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="workspace-page">
        <div className="workspace-heading">
          <div>
            <p className="step-label">Nueva sesión</p>
            <h1>¿Qué vas a estudiar?</h1>
            <p>
              Define el objetivo y Aslan preparará un entorno acorde a tu forma
              de trabajar.
            </p>
          </div>
          <span>{settings.pomodoroMinutes} min por ciclo</span>
        </div>
        <div className="setup-grid">
          <form
            className="workspace-card setup-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="field-group">
              <label htmlFor="subject">Materia</label>
              <div
                className={`input-shell ${
                  submitted && !subject ? "input-shell--error" : ""
                }`}
              >
                <select
                  id="subject"
                  value={subject}
                  onChange={(event) => {
                    setSubject(event.target.value)
                    setTopic("") // el tema anterior ya no aplica a la nueva materia
                  }}
                >
                  <option value="">Selecciona una materia</option>
                  {subjects
                    .filter((item) => !item.archived)
                    .map((item) => (
                      <option key={item.id}>{item.name}</option>
                    ))}
                </select>
              </div>
              {submitted && !subject && (
                <p className="field-error">Selecciona una materia.</p>
              )}
            </div>
            <div className="field-group">
  <label htmlFor="study-topic">Tema u objetivo</label>
  <div
    className={`input-shell ${
      submitted && !topic ? "input-shell--error" : ""
    }`}
  >
    <select
      id="study-topic"
      value={topic}
      disabled={!availableTopics.length}
      onChange={(event) => setTopic(event.target.value)}
    >
      <option value="">
        {!subject
          ? "Primero elige una materia"
          : availableTopics.length
            ? "Selecciona un tema"
            : "Esta materia aún no tiene temas"}
      </option>
      {availableTopics.map((item) => (
        <option key={item.id} value={item.name}>
          {item.name}
        </option>
      ))}
    </select>
  </div>
  {submitted && !topic && (
    <p className="field-error">Selecciona el tema que vas a estudiar.</p>
  )}
  {selectedSubject && !availableTopics.length && (
    <span className="field-hint">
      Crea un tema en Materias para poder usarlo aquí.
    </span>
  )}
</div>
            <div className="field-group">
              <label htmlFor="sound">Ambiente sonoro</label>
              <div className="input-shell">
                <select
                  id="sound"
                  value={sound}
                  onChange={(event) => setSound(event.target.value)}
                >
                  <option value="">Selecciona un ambiente</option>
                  <option>Silencio</option>
                  <option>Ruido marrón</option>
                  <option>Café tranquilo</option>
                  <option>Lluvia suave</option>
                  <option>Música instrumental</option>
                </select>
              </div>
            </div>
            <button className="submit-button" type="submit">
              Iniciar sesión<span aria-hidden="true">→</span>
            </button>
          </form>
          <aside
            className={`suggestion-panel ${
              suggestion ? "suggestion-panel--ready" : ""
            }`}
          >
            <span className="suggestion-icon">
              <SparkIcon />
            </span>
            {suggestion ? (
              <>
                <p className="suggestion-kicker">
                  {suggestion.personalized
                    ? "Sugerencia para ti"
                    : "Configuración estándar"}
                </p>
                <h2>{suggestion.technique}</h2>
                <p>{suggestion.reason}</p>
                <div className="suggestion-detail">
                  <span>Ambiente</span>
                  <strong>{suggestion.sound}</strong>
                </div>
                <div className="suggestion-detail">
                  <span>Duración</span>
                  <strong>{settings.pomodoroMinutes} minutos</strong>
                </div>
              </>
            ) : (
              <>
                <p className="suggestion-kicker">Preparando tu entorno</p>
                <h2>Completa materia y tema</h2>
                <p>
                  Cuando sepamos qué estudiarás, podremos sugerir una técnica y
                  un ambiente sonoro.
                </p>
              </>
            )}
          </aside>
        </div>
      </section>
    </main>
  )}

function PomodoroSettingsScreen({
  user,
  settings,
  onSaved,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onStudy,
  onProfile,
  onLogout,
}: {
  user: User
  settings: StudySettings
  onSaved: (settings: StudySettings) => void
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  const MIN_POMODORO = 15
  const MAX_POMODORO = 60

  const [minutes, setMinutes] = useState(String(settings.pomodoroMinutes))
  const [monitoring, setMonitoring] = useState(settings.monitoringEnabled)
  const toast = useToast()
  const parsedMinutes = Number(minutes)
  const valid =
    minutes.trim() !== "" &&
    Number.isInteger(parsedMinutes) &&
    parsedMinutes >= MIN_POMODORO &&
    parsedMinutes <= MAX_POMODORO

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key !== "Escape") return
      event.preventDefault()
      setMinutes(String(settings.pomodoroMinutes))
      setMonitoring(settings.monitoringEnabled)
    }
    window.addEventListener("keydown", onKeyDown)
    return () => window.removeEventListener("keydown", onKeyDown)
  }, [settings.pomodoroMinutes, settings.monitoringEnabled])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!valid) return
    const previous = settings
    const saved = await studyGateway.saveSettings(user.id, {
      pomodoroMinutes: parsedMinutes,
      monitoringEnabled: monitoring,
    })
    onSaved(saved)
    toast.show({
      tone: "success",
      message: "Configuración guardada.",
      actionLabel: "Deshacer",
      onAction: async () => {
        const restored = await studyGateway.saveSettings(user.id, {
          pomodoroMinutes: previous.pomodoroMinutes,
          monitoringEnabled: previous.monitoringEnabled,
        })
        onSaved(restored)
        setMinutes(String(restored.pomodoroMinutes))
        setMonitoring(restored.monitoringEnabled)
      },
    })
  }

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="settings"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={() => undefined}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="workspace-page settings-page">
        <div className="workspace-heading">
          <div>
            <p className="step-label">Preferencias</p>
            <h1>Configura tu Pomodoro</h1>
            <p>
              Ajusta el ritmo de trabajo y decide si Aslan puede analizar tu
              comportamiento durante las sesiones.
            </p>
          </div>
        </div>
        <form className="workspace-card settings-card" onSubmit={handleSubmit}>
          <div className="setting-row">
            <div>
              <h2>Duración del ciclo</h2>
              <p>Elige un bloque de trabajo entre 15 y 60 minutos.</p>
            </div>
            <div className="minutes-control">
              <input
                aria-label="Duración en minutos"
                type="number"
                min={MIN_POMODORO}
                max={MAX_POMODORO}
                value={minutes}
                onChange={(event) => setMinutes(event.target.value)}
              />
              <span>min</span>
            </div>
          </div>
          {!valid && (
            <p className="field-error settings-error">
              El valor debe estar entre {MIN_POMODORO} y {MAX_POMODORO} minutos.
            </p>
          )}
          <div className="profile-divider" />
          <div className="setting-row">
            <div>
              <h2>Monitoreo de comportamiento</h2>
              <p>
                Permite detectar cambios de pestaña para estimar tu
                concentración. No registra el contenido que visitas.
              </p>
            </div>
            <button
              type="button"
              role="switch"
              aria-checked={monitoring}
              className={`switch ${monitoring ? "switch--on" : ""}`}
              onClick={() => setMonitoring(!monitoring)}
            >
              <span />
            </button>
          </div>

          <div className="profile-actions">
            <button
              className="submit-button"
              type="submit"
              disabled={!valid}
              aria-disabled={!valid}
            >
              Guardar
            </button>
          </div>
        </form>
      </section>
    </main>
  )
}

function InterruptionDialog({
  onCancel,
  onSave,
}: {
  onCancel: () => void
  onSave: (reason?: string) => void
}) {
  const [reason, setReason] = useState("")
  const [custom, setCustom] = useState("")
  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="pause-title"
      >
        <span className="dialog-icon">Ⅱ</span>
        <h2 id="pause-title">Registrar una pausa</h2>
        <p>
          Indica qué interrumpió tu concentración. Puedes continuar sin
          especificar un motivo.
        </p>
        <div className="reason-list">
          {[
            "Notificación",
            "Llamada o mensaje",
            "Ruido externo",
            "Necesidad personal",
            "Otro",
          ].map((item) => (
            <button
              key={item}
              type="button"
              className={reason === item ? "is-selected" : ""}
              onClick={() => setReason(item)}
            >
              {item}
            </button>
          ))}
        </div>
        {reason === "Otro" && (
          <Field
            id="custom-reason"
            label="Motivo personalizado"
            placeholder="Escribe el motivo"
            value={custom}
            onChange={setCustom}
            autoComplete="off"
          />
        )}
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onCancel}>
            Cancelar
          </button>
          <button
            type="button"
            className="submit-button"
            onClick={() => onSave(reason === "Otro" ? custom : reason)}
          >
            Guardar pausa
          </button>
        </div>
      </section>
    </div>
  )
}

function FinishStudyDialog({
  onCancel,
  onDiscard,
  onSave,
}: {
  onCancel: () => void
  onDiscard: () => void
  onSave: () => void
}) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="finish-title"
      >
        <span className="dialog-icon">■</span>
        <h2 id="finish-title">¿Finalizar antes de tiempo?</h2>
        <p>
          Aún queda tiempo en el ciclo activo. Puedes guardar el progreso
          parcial para incluirlo en tus métricas.
        </p>
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onCancel}>
            Continuar estudiando
          </button>
          <button
            type="button"
            className="text-danger-button"
            onClick={onDiscard}
          >
            Salir sin guardar
          </button>
          <button type="button" className="submit-button" onClick={onSave}>
            Guardar y finalizar
          </button>
        </div>
      </section>
    </div>
  )
}

function StudySessionScreen({
  user,
  settings,
  initialSession,
  topicNotes,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onSettings,
  onProfile,
  onLogout,
  onFinished,
  onDiscard,
}: {
  user: User
  settings: StudySettings
  initialSession: StudySession
  onHome: () => void
  topicNotes?: string
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
  onFinished: (summary: SessionSummary) => void
  onDiscard: () => void
}) {
  const [session, setSession] = useState(initialSession)
  const [phase, setPhase] = useState<"ready" | "running" | "paused" | "break">(
    "ready",
  )
  const [secondsLeft, setSecondsLeft] = useState(settings.pomodoroMinutes * 60)
  const [interruptionOpen, setInterruptionOpen] = useState(false)
  const [finishOpen, setFinishOpen] = useState(false)
  const [concentration, setConcentration] = useState(
    settings.monitoringEnabled ? 86 : null,
  )
  const [adjusted, setAdjusted] = useState(false)
  const [analysisNotice, setAnalysisNotice] = useState<string | null>(null)
  const [notesOpen, setNotesOpen] = useState(false)

  async function finish(completed: boolean) {
    const summary = await studyGateway.finishSession(
      session,
      concentration,
      completed,
    )
    onFinished(summary)
  }

  function registerDistraction() {
    if (!settings.monitoringEnabled || phase !== "running") return
    setConcentration((value) => Math.max(30, (value ?? 86) - 12))
  }

  useEffect(() => {
    if (!settings.monitoringEnabled || phase !== "running") return
    const handleVisibility = () => {
      if (document.hidden) registerDistraction()
    }
    document.addEventListener("visibilitychange", handleVisibility)
    return () =>
      document.removeEventListener("visibilitychange", handleVisibility)
  }, [phase, settings.monitoringEnabled])

  useEffect(() => {
    if ((concentration ?? 100) > 55 || adjusted || phase !== "running") return
    setSecondsLeft((current) => Math.max(60, Math.floor(current * 0.8)))
    setAdjusted(true)
    setAnalysisNotice(
      "Detectamos fatiga. Acortamos este bloque y te sugerimos caminar o estirar durante 5 minutos.",
    )
  }, [adjusted, concentration, phase])

  useEffect(() => {
    if (phase !== "running" && phase !== "break") return
    const timer = window.setInterval(
      () => setSecondsLeft((current) => Math.max(0, current - 1)),
      1000,
    )
    return () => window.clearInterval(timer)
  }, [phase])

  useEffect(() => {
    if (secondsLeft !== 0) return
    if (phase === "running") {
      const completedCycles = session.completedCycles + 1
      const updated = { ...session, completedCycles }
      setSession(updated)
      if (completedCycles >= session.plannedCycles) {
        studyGateway
          .finishSession(updated, concentration, true)
          .then(onFinished)
      } else {
        setPhase("break")
        setSecondsLeft(completedCycles % 4 === 0 ? 15 * 60 : 5 * 60)
        setAnalysisNotice(null)
      }
    } else if (phase === "break") {
      setPhase("ready")
      setSecondsLeft(settings.pomodoroMinutes * 60)
      setAnalysisNotice("Descanso terminado. Tu siguiente ciclo está listo.")
    }
  }, [secondsLeft, phase])

  async function saveInterruption(reason?: string) {
    const interruption = await studyGateway.saveInterruption(session.id, reason)
    setSession((current) => ({
      ...current,
      interruptions: [...current.interruptions, interruption],
    }))
    setInterruptionOpen(false)
    setAnalysisNotice(null)
  }

  const minutes = String(Math.floor(secondsLeft / 60)).padStart(2, "0")
  const seconds = String(secondsLeft % 60).padStart(2, "0")
  const progress =
    phase === "break" ? 0 : 1 - secondsLeft / (settings.pomodoroMinutes * 60)

  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="study"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={() => undefined}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="session-page">
        <div className="session-topbar">
          <div>
            <span>{session.subject}</span>
            <strong>{session.topic}</strong>
          </div>
          <button
            type="button"
            className="secondary-button"
            onClick={() => setFinishOpen(true)}
          >
            Finalizar sesión
          </button>
        </div>
        {analysisNotice && (
          <div className="analysis-banner">
            <SparkIcon />
            <p>{analysisNotice}</p>
            <button
              type="button"
              onClick={() => setAnalysisNotice(null)}
              aria-label="Cerrar"
            >
              ×
            </button>
          </div>
        )}
        <div className="timer-layout">
          <section className="timer-card">
            <p className="timer-phase">
              {phase === "break"
                ? "Descanso"
                : phase === "paused"
                  ? "Ciclo pausado"
                  : "Concentración"}
            </p>
            <div className="timer-value">
              {minutes}
              <span>:</span>
              {seconds}
            </div>
            <div className="timer-track">
              <span style={{ width: `${Math.min(100, progress * 100)}%` }} />
            </div>
            <p className="cycle-count">
              Ciclo{" "}
              {Math.min(session.completedCycles + 1, session.plannedCycles)} de{" "}
              {session.plannedCycles} · {session.technique}
            </p>
            <div className="timer-actions">
              {phase === "ready" && (
                <button
                  className="submit-button"
                  type="button"
                  onClick={() => setPhase("running")}
                >
                  Iniciar ciclo
                </button>
              )}
              {phase === "paused" && (
                <button
                  className="submit-button"
                  type="button"
                  onClick={() => setPhase("running")}
                >
                  Reanudar
                </button>
              )}
              {phase === "running" && (
                <button
                  className="submit-button"
                  type="button"
                  onClick={() => setPhase("paused")}
                >
                  Pausar
                </button>
              )}
              {phase === "break" && (
                <button
                  className="submit-button"
                  type="button"
                  onClick={() => {
                    setPhase("ready")
                    setSecondsLeft(settings.pomodoroMinutes * 60)
                  }}
                >
                  Omitir descanso
                </button>
              )}
              {(phase === "running" || phase === "paused") && (
                <button
                  className="secondary-button"
                  type="button"
                  onClick={() => {
                    setPhase("paused")
                    setInterruptionOpen(true)
                  }}
                >
                  Registrar pausa
                </button>
              )}
            </div>
          </section>
          <aside className="session-side">
            <div className="session-info-card notes-card">
              <span>Notas del tema</span>
              <strong>
                {topicNotes ? "Apuntes disponibles" : "Sin notas"}
              </strong>
              <button type="button" onClick={() => setNotesOpen(true)}>
                Abrir notas
              </button>
            </div>
            <div className="session-info-card">
              <span>Ambiente sonoro</span>
              <strong>{session.sound}</strong>
              <p>Configuración aplicada al iniciar la sesión.</p>
            </div>
            <div className="concentration-card">
              <div>
                <span>Concentración</span>
                <i className={settings.monitoringEnabled ? "is-active" : ""}>
                  {settings.monitoringEnabled ? "Monitoreando" : "Desactivado"}
                </i>
              </div>
              {settings.monitoringEnabled && (
                <>
                  <strong>{concentration}%</strong>
                  <div className="concentration-track">
                    <span style={{ width: `${concentration}%` }} />
                  </div>
                  <button type="button" onClick={registerDistraction}>
                    Simular cambio de pestaña
                  </button>
                </>
              )}
            </div>
            <div className="session-info-card">
              <span>Pausas registradas</span>
              <strong>{session.interruptions.length}</strong>
              <p>
                {session.interruptions.at(-1)?.reason ??
                  "Aún no registraste interrupciones."}
              </p>
            </div>
          </aside>
        </div>
      </section>
      {interruptionOpen && (
        <InterruptionDialog
          onCancel={() => setInterruptionOpen(false)}
          onSave={saveInterruption}
        />
      )}
      {finishOpen && (
        <FinishStudyDialog
          onCancel={() => setFinishOpen(false)}
          onDiscard={onDiscard}
          onSave={() => finish(false)}
        />
      )}
      {notesOpen && (
        <div className="modal-backdrop notes-backdrop" role="presentation">
          <section
            className="dialog notes-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="notes-title"
          >
            <p className="step-label">Notas de estudio</p>
            <h2 id="notes-title">{session.topic}</h2>
            <div className="notes-content">
              {topicNotes || "Este tema todavía no tiene notas asociadas."}
            </div>
            <div className="dialog-actions">
              <button
                type="button"
                className="submit-button"
                onClick={() => setNotesOpen(false)}
              >
                Volver al Pomodoro
              </button>
            </div>
          </section>
        </div>
      )}
    </main>
  )
}

function SessionSummaryScreen({
  user,
  summary,
  onHome,
  onSubjects,
  onReviews,
  onNotifications,
  onPlan,
  onStudy,
  onSettings,
  onProfile,
  onLogout,
}: {
  user: User
  summary: SessionSummary
  onHome: () => void
  onSubjects: () => void
  onReviews: () => void
  onNotifications: () => void
  onPlan: () => void
  onStudy: () => void
  onSettings: () => void
  onProfile: () => void
  onLogout: () => void
}) {
  return (
    <main className="dashboard">
      <AppHeader
        user={user}
        activeSection="study"
        onHome={onHome}
        onSubjects={onSubjects}
        onReviews={onReviews}
        onNotifications={onNotifications}
        onPlan={onPlan}
        onStudy={onStudy}
        onSettings={onSettings}
        onProfile={onProfile}
        onLogout={onLogout}
      />
      <section className="summary-page">
        <span className="summary-check">
          <CheckIcon />
        </span>
        <p className="step-label">Sesión registrada</p>
        <h1>
          {summary.completed ? "Completaste tu sesión" : "Progreso guardado"}
        </h1>
        <p className="summary-lead">
          Aslan procesó tus métricas y actualizó tu perfil de estudio.
        </p>
        <div className="metrics-grid">
          <div>
            <span>Tiempo estudiado</span>
            <strong>{summary.elapsedMinutes} min</strong>
          </div>
          <div>
            <span>Ciclos completos</span>
            <strong>{summary.completedCycles}</strong>
          </div>
          <div>
            <span>Pausas</span>
            <strong>{summary.interruptions}</strong>
          </div>
          <div>
            <span>Concentración</span>
            <strong>
              {summary.averageConcentration === null
                ? "Sin datos"
                : `${summary.averageConcentration}%`}
            </strong>
          </div>
        </div>
        <div className="recommendation-card">
          <SparkIcon />
          <div>
            <span>Recomendación para tu próxima sesión</span>
            <p>{summary.recommendation}</p>
            <small>
              {summary.modelUpdated
                ? "Tu modelo personal ya fue actualizado con esta sesión."
                : "Actualización pendiente."}
            </small>
          </div>
        </div>
        <div className="summary-actions">
          <button type="button" className="secondary-button" onClick={onHome}>
            Volver al inicio
          </button>
          <button type="button" className="submit-button" onClick={onStudy}>
            Preparar otra sesión
          </button>
        </div>
      </section>
    </main>
  )
}

function LogoutDialog({
  hasPomodoro,
  onCancel,
  onConfirm,
}: {
  hasPomodoro: boolean
  onCancel: () => void
  onConfirm: (save: boolean) => void
}) {
  return (
    <div className="modal-backdrop" role="presentation">
      <section
        className="dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="logout-title"
      >
        <span className="dialog-icon">{hasPomodoro ? "Ⅱ" : "→"}</span>
        <h2 id="logout-title">
          {hasPomodoro
            ? "Tienes una sesión en curso"
            : "¿Quieres cerrar sesión?"}
        </h2>
        <p>
          {hasPomodoro
            ? "Puedes guardar el progreso de este Pomodoro antes de salir para retomarlo después."
            : "Tendrás que volver a ingresar tus credenciales para acceder a tu espacio."}
        </p>
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onCancel}>
            Cancelar
          </button>
          {hasPomodoro && (
            <button
              type="button"
              className="text-danger-button"
              onClick={() => onConfirm(false)}
            >
              Salir sin guardar
            </button>
          )}
          <button
            type="button"
            className="submit-button"
            onClick={() => onConfirm(true)}
          >
            {hasPomodoro ? "Guardar y salir" : "Cerrar sesión"}
          </button>
        </div>
      </section>
    </div>
  )
}

function ReviewNotificationToast({
  title,
  detail,
  onOpen,
  onClose,
}: {
  title: string
  detail: string
  onOpen: () => void
  onClose: () => void
}) {
  return (
    <aside className="review-toast" role="status">
      <button type="button" className="review-toast-main" onClick={onOpen}>
        <span className="review-toast-icon">
          <SparkIcon />
        </span>
        <span>
          <strong>{title}</strong>
          <small>{detail}</small>
        </span>
      </button>
      <button
        type="button"
        className="review-toast-close"
        onClick={onClose}
        aria-label="Cerrar notificación"
      >
        ×
      </button>
    </aside>
  )
}

export default function App() {
  const [route, setRoute] = useState<Route>("login")
  const routeHistory = useRef<Route[]>([])
  const [user, setUser] = useState<User | null>(null)
  const [settings, setSettings] = useState<StudySettings>({
    pomodoroMinutes: 25,
    monitoringEnabled: true,
  })
  const [activeSession, setActiveSession] = useState<StudySession | null>(null)
  const [summary, setSummary] = useState<SessionSummary | null>(null)
  const [subjects, setSubjects] = useState<Subject[]>([])
  const [selectedSubjectId, setSelectedSubjectId] = useState<string | null>(
    null,
  )
  const [weeklyPlan, setWeeklyPlan] = useState<WeeklyPlan | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [reviewNotifications, setReviewNotifications] = useState(true)
  const [focusedReviewId, setFocusedReviewId] = useState<string | null>(null)
  const [reviewNotification, setReviewNotification] = useState<{
    title: string
    detail: string
    reviewId?: string
    kind?: "scheduled"
    topics?: string[]
  } | null>(null)
  const [dueNotificationShown, setDueNotificationShown] = useState(false)
  const [activityVersion, setActivityVersion] = useState(0)
  const [contextNotifications, setContextNotifications] =
    useState<ContextNotification[]>([])
  const [notificationPreferences, setNotificationPreferences] =
    useState<NotificationPreferences>({
      frequency: "inmediata",
      reminders: true,
      motivation: true,
      alerts: true,
      system: true,
    })
  const [recommendations, setRecommendations] =
    useState<ContextRecommendation[]>([])
  const [contextToast, setContextToast] = useState<{
    title: string
    detail: string
    action: ContextNotification["action"]
    id?: string
  } | null>(null)
  const [contextToastShown, setContextToastShown] = useState(false)
  const [logoutOpen, setLogoutOpen] = useState(false)

  async function refreshCatalog() {
    if (!user) return
    setSubjects(await catalogGateway.listSubjects(user.id))
  }

  async function refreshReviews() {
    if (!user) return
    setReviews(await reviewGateway.list(user.id))
  }

  async function refreshContextNotifications() {
    if (!user) return
    setContextNotifications(await notificationGateway.list(user.id))
  }

  async function respondToRecommendation(
    id: string,
    response: "accepted" | "rejected" | "ignored",
  ) {
    await notificationGateway.respondToRecommendation(id, response)
    if (!user) return
    setRecommendations(await notificationGateway.getRecommendations(user.id))
    if (response === "accepted") {
      setContextToast({
        title: "Recomendación aplicada",
        detail: "Aslan tendrá en cuenta esta decisión en futuras sugerencias.",
        action: "home",
      })
    }
  }

  useEffect(() => {
    if (user) catalogGateway.listSubjects(user.id).then(setSubjects)
    else setSubjects([])
  }, [user])

  useEffect(() => {
    if (user) reviewGateway.list(user.id).then(setReviews)
    else setReviews([])
  }, [user])

  useEffect(() => {
    if (!user) {
      setContextNotifications([])
      setRecommendations([])
      return
    }
    Promise.all([
      notificationGateway.list(user.id),
      notificationGateway.getPreferences(user.id),
      notificationGateway.getRecommendations(user.id),
    ]).then(([items, savedPreferences, savedRecommendations]) => {
      setContextNotifications(items)
      setNotificationPreferences(savedPreferences)
      setRecommendations(savedRecommendations)
    })
  }, [user])

  useEffect(() => {
    if (contextToastShown || !contextNotifications.length) return
    const preferenceKey: Record<ContextNotification["type"], keyof NotificationPreferences> =
      {
        Recordatorio: "reminders",
        Motivación: "motivation",
        Alerta: "alerts",
        Sistema: "system",
      }
    const unread = contextNotifications.filter(
      (notification) =>
        !notification.read &&
        Boolean(notificationPreferences[preferenceKey[notification.type]]),
    )
    if (!unread.length) return
    if (notificationPreferences.frequency === "diaria") {
      setContextToast({
        title: `Resumen diario · ${unread.length} ${
          unread.length === 1 ? "aviso" : "avisos"
        }`,
        detail: unread.map((notification) => notification.title).join(", "),
        action: "home",
      })
    } else {
      setContextToast({
        title: unread[0].title,
        detail: unread[0].detail,
        action: unread[0].action,
        id: unread[0].id,
      })
    }
    setContextToastShown(true)
  }, [contextNotifications, contextToastShown, notificationPreferences])

  useEffect(() => {
    if (!reviewNotifications || dueNotificationShown || !reviews.length) return
    const tomorrow = new Date(Date.now() + 86_400_000)
      .toISOString()
      .slice(0, 10)
    const due = reviews.filter(
      (review) => review.status !== "Completado" && review.dueDate <= tomorrow,
    )
    if (!due.length) return
    setReviewNotification({
      title:
        due.length === 1
          ? "Repaso próximo a vencer"
          : `${due.length} repasos requieren atención`,
      detail:
        due.length === 1
          ? `${due[0].topicName} · ${due[0].subjectName}`
          : due
              .map((review) => review.topicName)
              .slice(0, 3)
              .join(", "),
      reviewId: due[0].id,
    })
    setDueNotificationShown(true)
  }, [dueNotificationShown, reviewNotifications, reviews])

  useEffect(() => {
    if (!user || weeklyPlan || new Date().getDay() !== 1 || !subjects.length)
      return
    plannerGateway
      .generate(subjects, settings.pomodoroMinutes, reviews)
      .then((generated) => {
        if (generated) setWeeklyPlan(generated)
      })
  }, [reviews, settings.pomodoroMinutes, subjects, user, weeklyPlan])

  async function scheduleTopicReview(subject: Subject, topic: Topic) {
    const review = await reviewGateway.scheduleInitial({
      subjectId: subject.id,
      subjectName: subject.name,
      topicId: topic.id,
      topicName: topic.name,
    })
    await refreshReviews()
    if (reviewNotifications) {
      setReviewNotification((current) => {
        const topics =
          current?.kind === "scheduled"
            ? [...(current.topics ?? []), review.topicName]
            : [review.topicName]
        return {
          kind: "scheduled",
          topics,
          title:
            topics.length === 1
              ? "Nuevo repaso programado"
              : `${topics.length} nuevos repasos programados`,
          detail:
            topics.length === 1
              ? `${review.topicName} · ${new Date(`${review.dueDate}T12:00:00`).toLocaleDateString("es", { day: "numeric", month: "long" })}`
              : topics.join(", "),
          reviewId: review.id,
        }
      })
    }
  }

  function navigate(next: Route) {
    if (!["login", "register"].includes(next) && !user) {
      setRoute("login")
      return
    }
    if (next !== route) {
      routeHistory.current = [...routeHistory.current.slice(-19), route]
    }
    setRoute(next)
  }

  function goBack() {
    const previous = routeHistory.current.pop()
    setRoute(previous && previous !== "login" ? previous : "home")
  }

  function requestLogout() {
    setLogoutOpen(true)
  }

  async function confirmLogout(saveProgress: boolean) {
    if (saveProgress && activeSession) {
      await studyGateway.finishSession(activeSession, null, false)
    }
    await authGateway.logout()
    setUser(null)
    setActiveSession(null)
    setSummary(null)
    setSelectedSubjectId(null)
    setWeeklyPlan(null)
    setReviews([])
    setFocusedReviewId(null)
    setReviewNotification(null)
    setDueNotificationShown(false)
    setContextNotifications([])
    setRecommendations([])
    setContextToast(null)
    setContextToastShown(false)
    routeHistory.current = []
    setLogoutOpen(false)
    setRoute("login")
  }

  return (
    <>
      {route === "login" && (
        <LoginScreen
          onLogin={(loggedUser) => {
            setUser(loggedUser)
            setRoute("home")
          }}
          onRegister={() => navigate("register")}
        />
      )}
      {route === "register" && (
        <RegisterScreen
          onLogin={(registeredUser) => {
            setUser(registeredUser)
            setRoute("home")
          }}
          onLoginScreen={() => navigate("login")}
        />
      )}
      {route === "home" && user && (
        <HomeScreen
          user={user}
          settings={settings}
          subjects={subjects}
          plan={weeklyPlan}
          activityVersion={activityVersion}
          recommendations={recommendations}
          onRecommendation={respondToRecommendation}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onStudy={() => navigate("study")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
        />
      )}
      {route === "profile" && user && (
        <ProfileScreen
          user={user}
          onUpdated={setUser}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onStudy={() => navigate("study")}
          onSettings={() => navigate("settings")}
          onLogout={requestLogout}
        />
      )}
      {route === "subjects" && user && (
        <SubjectsScreen
          user={user}
          subjects={subjects}
          selectedId={selectedSubjectId}
          onSelect={setSelectedSubjectId}
          onRefresh={refreshCatalog}
          onTopicCompleted={scheduleTopicReview}
          onBack={() => {
            setSelectedSubjectId(null)
            goBack()
          }}
          onHome={() => navigate("home")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onStudy={() => navigate("study")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
        />
      )}
      {route === "reviews" && user && (
        <ReviewsScreen
          user={user}
          subjects={subjects}
          reviews={reviews}
          notificationsEnabled={reviewNotifications}
          onNotificationsChange={setReviewNotifications}
          focusedReviewId={focusedReviewId}
          onRefresh={refreshReviews}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onContextNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onStudy={() => navigate("study")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
          onNotify={(title, detail, reviewId) =>
            setReviewNotification({ title, detail, reviewId })
          }
        />
      )}
      {route === "notifications" && user && (
        <NotificationCenterScreen
          user={user}
          notifications={contextNotifications}
          preferences={notificationPreferences}
          onPreferencesChange={(next) => {
            setNotificationPreferences(next)
            setContextToastShown(false)
          }}
          onRefresh={refreshContextNotifications}
          onNavigate={(action) => navigate(action)}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onPlan={() => navigate("plan")}
          onStudy={() => navigate("study")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
        />
      )}
      {route === "plan" && user && (
        <WeeklyPlanScreen
          user={user}
          subjects={subjects}
          reviews={reviews}
          settings={settings}
          plan={weeklyPlan}
          onPlanChange={setWeeklyPlan}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onStudy={() => navigate("study")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
        />
      )}
      {route === "study" && user && !activeSession && (
        <StudySetupScreen
          user={user}
          settings={settings}
          subjects={subjects}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
          onStart={(session) => {
            setActiveSession(session)
            setRoute("study")
          }}
        />
      )}
      {route === "study" && user && activeSession && (
        <StudySessionScreen
          user={user}
          settings={settings}
          initialSession={activeSession}
          topicNotes={
            subjects
              .flatMap((subject) => subject.topics)
              .find((topic) => topic.name === activeSession.topic)?.notes
          }
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
          onFinished={async (result) => {
            await analyticsGateway.recordSession(user.id, result)
            setActivityVersion((current) => current + 1)
            setSummary(result)
            setActiveSession(null)
            setRoute("summary")
          }}
          onDiscard={() => {
            setActiveSession(null)
            setRoute("home")
          }}
        />
      )}
      {route === "settings" && user && (
        <PomodoroSettingsScreen
          user={user}
          settings={settings}
          onSaved={setSettings}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onStudy={() => navigate("study")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
        />
      )}
      {route === "summary" && user && summary && (
        <SessionSummaryScreen
          user={user}
          summary={summary}
          onHome={() => navigate("home")}
          onSubjects={() => navigate("subjects")}
          onReviews={() => navigate("reviews")}
          onNotifications={() => navigate("notifications")}
          onPlan={() => navigate("plan")}
          onStudy={() => {
            setSummary(null)
            navigate("study")
          }}
          onSettings={() => navigate("settings")}
          onProfile={() => navigate("profile")}
          onLogout={requestLogout}
        />
      )}
      {logoutOpen && (
        <LogoutDialog
          hasPomodoro={Boolean(activeSession)}
          onCancel={() => setLogoutOpen(false)}
          onConfirm={confirmLogout}
        />
      )}
      {reviewNotification && user && (
        <ReviewNotificationToast
          title={reviewNotification.title}
          detail={reviewNotification.detail}
          onClose={() => setReviewNotification(null)}
          onOpen={() => {
            setFocusedReviewId(reviewNotification.reviewId ?? null)
            setReviewNotification(null)
            navigate("reviews")
          }}
        />
      )}
      {contextToast && !reviewNotification && user && (
        <ReviewNotificationToast
          title={contextToast.title}
          detail={contextToast.detail}
          onClose={() => setContextToast(null)}
          onOpen={async () => {
            if (contextToast.id)
              await notificationGateway.markRead(contextToast.id)
            setContextToast(null)
            refreshContextNotifications()
            navigate(contextToast.action)
          }}
        />
      )}
    </>
  )
}
