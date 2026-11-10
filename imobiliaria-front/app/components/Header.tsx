"use client";

import Link from "next/link";
import { useAuth } from "@/app/context/AuthContext";

export default function Header() {
  const { sessao, carregando, sair } = useAuth();

  return (
    <header className="sticky top-0 z-30 border-b border-stone-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-5 py-4">
        <Link href="/" className="text-xl font-extrabold tracking-tight text-emerald-950">
          Imobiliária
        </Link>
        <nav aria-label="Menu principal" className="flex items-center gap-2 text-sm font-semibold">
          <Link href="/" className="rounded-lg px-3 py-2 hover:bg-stone-100">Imóveis</Link>
          {!carregando && !sessao && (
            <Link href="/login" className="rounded-lg bg-emerald-900 px-4 py-2 text-white hover:bg-emerald-800">Entrar</Link>
          )}
          {sessao?.tipo === "USER" && (
            <Link href="/cliente" className="rounded-lg px-3 py-2 hover:bg-stone-100">Minha área</Link>
          )}
          {sessao?.tipo === "ADMIN" && (
            <Link href="/admin" className="rounded-lg px-3 py-2 hover:bg-stone-100">Painel</Link>
          )}
          {sessao && (
            <button onClick={sair} className="cursor-pointer rounded-lg border border-stone-300 px-3 py-2 hover:bg-stone-100">Sair</button>
          )}
        </nav>
      </div>
    </header>
  );
}
