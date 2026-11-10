"use client";

import { FormEvent, Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { api } from "@/app/lib/api";
import { useAuth } from "@/app/context/AuthContext";
import type { Sessao } from "@/app/types";

function LoginForm() {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState("");
  const [enviando, setEnviando] = useState(false);
  const { entrar } = useAuth();
  const router = useRouter();
  const params = useSearchParams();

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setErro("");
    if (!email.trim() || !senha) return setErro("Preencha email e senha.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return setErro("Informe um email válido.");
    setEnviando(true);
    try {
      const sessao = await api<Sessao>("/public/login", {
        method: "POST",
        body: JSON.stringify({ email: email.trim(), senha }),
      });
      entrar(sessao);
      router.replace(sessao.tipo === "ADMIN" ? "/admin" : "/cliente");
    } catch (e) {
      setErro(e instanceof Error ? e.message : "Não foi possível entrar.");
    } finally {
      setEnviando(false);
    }
  }

  function preencher(tipo: "ADMIN" | "USER") {
    setEmail(tipo === "ADMIN" ? "admin@imobiliaria.com" : "cliente@imobiliaria.com");
    setSenha("1234");
    setErro("");
  }

  return (
    <main className="flex min-h-[calc(100vh-73px)] items-center justify-center bg-stone-100 px-5 py-12">
      <div className="w-full max-w-md rounded-2xl border border-stone-200 bg-white p-7 shadow-lg">
        <p className="text-sm font-bold uppercase tracking-widest text-emerald-800">Área privada</p>
        <h1 className="mt-2 text-3xl font-extrabold">Entrar</h1>
        <p className="mt-2 text-stone-600">Use seu email e senha para continuar.</p>
        {params.get("expirada") && <p role="alert" className="mt-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-900">Sua sessão expirou. Entre novamente.</p>}
        {erro && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-800">{erro}</p>}
        <form onSubmit={enviar} noValidate className="mt-6 space-y-4">
          <label className="block text-sm font-semibold">Email
            <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" autoComplete="email" className="mt-2 w-full rounded-lg border border-stone-300 p-3 font-normal" />
          </label>
          <label className="block text-sm font-semibold">Senha
            <input value={senha} onChange={(e) => setSenha(e.target.value)} type="password" autoComplete="current-password" className="mt-2 w-full rounded-lg border border-stone-300 p-3 font-normal" />
          </label>
          <button disabled={enviando} className="w-full cursor-pointer rounded-lg bg-emerald-900 p-3 font-bold text-white disabled:opacity-60">{enviando ? "Entrando…" : "Entrar"}</button>
        </form>
        <div className="mt-6 border-t border-stone-200 pt-5">
          <p className="mb-3 text-xs font-bold uppercase tracking-wide text-stone-500">Acessos de demonstração</p>
          <div className="grid grid-cols-2 gap-3">
            <button onClick={() => preencher("USER")} className="cursor-pointer rounded-lg border border-stone-300 px-3 py-2 text-sm">Preencher cliente</button>
            <button onClick={() => preencher("ADMIN")} className="cursor-pointer rounded-lg border border-stone-300 px-3 py-2 text-sm">Preencher admin</button>
          </div>
        </div>
      </div>
    </main>
  );
}

export default function LoginPage() {
  return <Suspense fallback={<main className="mx-auto max-w-6xl px-5 py-16">Carregando login…</main>}><LoginForm /></Suspense>;
}
