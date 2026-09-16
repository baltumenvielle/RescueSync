import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router'
import type { Rol } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { ROLES } from '../lib/formato'
import { cx } from '../lib/cx'

interface ItemNav {
  to: string
  texto: string
  roles: Rol[]
  exacto?: boolean
}

const NAVEGACION: ItemNav[] = [
  { to: '/tareas', texto: 'Bandeja de tareas', roles: ['OPERADOR_MUNICIPAL', 'CCR', 'REPRESENTANTE_ONG'] },
  { to: '/emergencias/nueva', texto: 'Nueva emergencia', roles: ['OPERADOR_MUNICIPAL'] },
  { to: '/emergencias', texto: 'Emergencias', roles: ['OPERADOR_MUNICIPAL', 'CCR', 'AUDITOR'], exacto: true },
  { to: '/ong/convocatorias', texto: 'Convocatorias abiertas', roles: ['REPRESENTANTE_ONG'] },
  { to: '/ong/ofertas', texto: 'Mis ofertas', roles: ['REPRESENTANTE_ONG'] },
  { to: '/auditoria/eventos', texto: 'Eventos de auditoría', roles: ['AUDITOR'] },
]

function Logo() {
  return (
    <span className="flex items-center gap-2">
      <svg viewBox="0 0 32 32" className="size-8" aria-hidden>
        <rect width="32" height="32" rx="7" fill="#f97316" />
        <path d="M13 7h6v6h6v6h-6v6h-6v-6H7v-6h6z" fill="#fff" />
      </svg>
      <span className="text-lg font-bold tracking-tight text-white">RescueSync</span>
    </span>
  )
}

export function Layout() {
  const { usuario, logout } = useAuth()
  const location = useLocation()
  // El menú móvil queda abierto solo para la ruta en la que se abrió: navegar lo cierra.
  const [menuAbiertoEn, setMenuAbiertoEn] = useState<string | null>(null)
  const menuAbierto = menuAbiertoEn === location.pathname

  if (!usuario) return null
  const items = NAVEGACION.filter((i) => i.roles.includes(usuario.rol))

  const enlaces = (
    <nav className="flex flex-col gap-1" aria-label="Principal">
      {items.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.exacto}
          className={({ isActive }) =>
            cx(
              'rounded-lg px-3 py-2.5 text-sm font-medium transition',
              isActive ? 'bg-white/10 text-white' : 'text-slate-300 hover:bg-white/5 hover:text-white',
            )
          }
        >
          {item.texto}
        </NavLink>
      ))}
    </nav>
  )

  const perfil = (
    <div className="border-t border-white/10 pt-4">
      <p className="truncate text-sm font-semibold text-white">{usuario.nombre}</p>
      <p className="truncate text-xs text-slate-400">{usuario.organizacion.nombre}</p>
      <p className="mt-1 inline-block rounded bg-marca-500/15 px-1.5 py-0.5 text-[11px] font-medium text-marca-100">
        {ROLES[usuario.rol]}
      </p>
      <button
        type="button"
        onClick={logout}
        className="mt-3 block w-full rounded-lg px-3 py-2 text-left text-sm font-medium text-slate-300 hover:bg-white/5 hover:text-white"
      >
        Cerrar sesión
      </button>
    </div>
  )

  return (
    <div className="min-h-dvh lg:flex">
      {/* Barra superior en pantallas chicas y medianas */}
      <header className="sticky top-0 z-30 flex items-center justify-between bg-slate-900 px-4 py-3 lg:hidden">
        <Logo />
        <button
          type="button"
          className="rounded-lg p-2 text-slate-200 hover:bg-white/10"
          aria-expanded={menuAbierto}
          aria-controls="menu-movil"
          onClick={() => setMenuAbiertoEn(menuAbierto ? null : location.pathname)}
        >
          <span className="sr-only">{menuAbierto ? 'Cerrar menú' : 'Abrir menú'}</span>
          <svg viewBox="0 0 24 24" className="size-6" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
            {menuAbierto ? <path d="M6 6l12 12M18 6L6 18" /> : <path d="M4 7h16M4 12h16M4 17h16" />}
          </svg>
        </button>
      </header>
      {menuAbierto && (
        <div id="menu-movil" className="fixed inset-x-0 top-[60px] bottom-0 z-20 flex flex-col gap-4 overflow-y-auto bg-slate-900 px-4 pb-6 pt-2 lg:hidden">
          {enlaces}
          {perfil}
        </div>
      )}

      {/* Barra lateral en escritorio */}
      <aside className="hidden lg:sticky lg:top-0 lg:flex lg:h-dvh lg:w-64 lg:shrink-0 lg:flex-col lg:justify-between lg:bg-slate-900 lg:px-4 lg:py-5">
        <div className="flex flex-col gap-6">
          <div className="px-2">
            <Logo />
          </div>
          {enlaces}
        </div>
        {perfil}
      </aside>

      <main className="min-w-0 flex-1 px-4 py-5 sm:px-6 sm:py-8 lg:px-10">
        <div className="mx-auto max-w-6xl">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
