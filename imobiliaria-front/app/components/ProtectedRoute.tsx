"use client";

import { ReactNode, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/app/context/AuthContext";
import { areaDoPerfil, type Perfil } from "@/app/types";

export default function ProtectedRoute({ perfil, children }: { perfil: Perfil; children: ReactNode }) {
  const { sessao, carregando } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!carregando && !sessao) router.replace(sessionStorage.getItem("imobiliaria_expirada") ? "/login?expirada=1" : "/login");
    else if (!carregando && sessao && sessao.tipo !== perfil) router.replace(areaDoPerfil(sessao.tipo));
  }, [carregando, perfil, router, sessao]);

  if (carregando || !sessao || sessao.tipo !== perfil) {
    return <main className="mx-auto max-w-6xl px-5 py-16" role="status">Verificando acesso…</main>;
  }
  return children;
}
