"use client";

import { createContext, ReactNode, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, ApiError, limparSessao } from "@/app/lib/api";
import type { Sessao } from "@/app/types";

type AuthContextValue = {
  sessao: Sessao | null;
  carregando: boolean;
  entrar: (sessao: Sessao) => void;
  sair: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function tokenExpirou(token: string) {
  try {
    const parte = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const payload = JSON.parse(atob(parte)) as { exp?: number };
    return !payload.exp || payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(null);
  const [carregando, setCarregando] = useState(true);
  const router = useRouter();

  useEffect(() => {
    let ativo = true;
    const controller = new AbortController();
    const sessaoExpirada = () => {
      sessionStorage.setItem("imobiliaria_expirada", "1");
      setSessao(null);
      router.replace("/login?expirada=1");
    };
    window.addEventListener("imobiliaria:sessao-expirada", sessaoExpirada);
    const temporizador = window.setTimeout(async () => {
      const salva = localStorage.getItem("imobiliaria_sessao");
      if (salva) {
        try {
          const lida = JSON.parse(salva) as Sessao;
          if (!tokenExpirou(lida.token)) {
            const atual = await api<Omit<Sessao, "token">>("/sessao", { signal: controller.signal });
            if (ativo) setSessao({ ...atual, token: lida.token });
          } else {
            limparSessao();
            sessionStorage.setItem("imobiliaria_expirada", "1");
            router.replace("/login?expirada=1");
          }
        } catch (e) {
          if (!ativo) return;
          if (!(e instanceof ApiError) || e.status === 401) limparSessao();
        }
      }
      if (ativo) setCarregando(false);
    }, 0);
    return () => {
      ativo = false;
      controller.abort();
      window.clearTimeout(temporizador);
      window.removeEventListener("imobiliaria:sessao-expirada", sessaoExpirada);
    };
  }, [router]);

  useEffect(() => {
    if (!sessao) return;
    const parte = sessao.token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const exp = (JSON.parse(atob(parte)) as { exp: number }).exp;
    const timer = window.setTimeout(() => {
      limparSessao();
      sessionStorage.setItem("imobiliaria_expirada", "1");
      setSessao(null);
      router.replace("/login?expirada=1");
    }, Math.max(0, exp * 1000 - Date.now()));
    return () => window.clearTimeout(timer);
  }, [sessao, router]);

  function entrar(novaSessao: Sessao) {
    sessionStorage.removeItem("imobiliaria_expirada");
    localStorage.setItem("imobiliaria_sessao", JSON.stringify(novaSessao));
    document.cookie = `imobiliaria_token=${novaSessao.token}; path=/; max-age=3600; samesite=lax`;
    document.cookie = `imobiliaria_tipo=${novaSessao.tipo}; path=/; max-age=3600; samesite=lax`;
    setSessao(novaSessao);
  }

  function sair() {
    sessionStorage.removeItem("imobiliaria_expirada");
    limparSessao();
    setSessao(null);
    router.replace("/login");
  }

  return <AuthContext.Provider value={{ sessao, carregando, entrar, sair }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) throw new Error("useAuth deve ser usado dentro de AuthProvider");
  return contexto;
}
