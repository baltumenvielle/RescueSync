import { useEffect, useRef, useState } from 'react'
import { cx } from '../lib/cx'

function partes(ms: number) {
  const total = Math.max(0, Math.floor(ms / 1000))
  return {
    dias: Math.floor(total / 86400),
    horas: Math.floor((total % 86400) / 3600),
    minutos: Math.floor((total % 3600) / 60),
    segundos: total % 60,
  }
}

/** Tiempo restante hasta el cierre de la ventana de ofertas. */
export function CuentaRegresiva({ hasta, alVencer, className }: { hasta: string; alVencer?: () => void; className?: string }) {
  const objetivo = new Date(hasta).getTime()
  const [ahora, setAhora] = useState(() => Date.now())
  const restante = objetivo - ahora
  const vencida = restante <= 0

  // Se guarda el callback en una ref para notificar una sola vez, aunque cambie su identidad.
  const alVencerRef = useRef(alVencer)
  useEffect(() => {
    alVencerRef.current = alVencer
  })

  useEffect(() => {
    if (vencida) {
      alVencerRef.current?.()
      return
    }
    const t = setInterval(() => setAhora(Date.now()), 1000)
    return () => clearInterval(t)
  }, [vencida])

  if (vencida) {
    return <span className={cx('font-semibold text-red-700', className)}>Ventana cerrada</span>
  }
  const { dias, horas, minutos, segundos } = partes(restante)
  const dos = (n: number) => String(n).padStart(2, '0')
  const urgente = restante < 3600_000
  return (
    <span className={cx('font-mono font-semibold tabular-nums', urgente ? 'text-red-700' : 'text-slate-900', className)}>
      {dias > 0 && `${dias}d `}
      {dos(horas)}:{dos(minutos)}:{dos(segundos)}
    </span>
  )
}
