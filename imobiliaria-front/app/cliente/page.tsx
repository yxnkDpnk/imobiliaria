"use client";

import ProtectedRoute from "@/app/components/ProtectedRoute";
import { useAuth } from "@/app/context/AuthContext";

export default function ClientePage() {
  const { sessao } = useAuth();
  return (
    <ProtectedRoute perfil="USER">
      <main className="mx-auto w-full max-w-6xl flex-1 px-5 py-12">
        <p className="text-sm font-bold uppercase tracking-widest text-emerald-800">Área do cliente</p>
        <h1 className="mt-2 text-3xl font-extrabold">Olá, {sessao?.nome}</h1>
        <p className="mt-3 text-stone-600">Seu acesso está protegido por JWT e foi reconhecido como cliente.</p>
        <div className="mt-8 grid gap-5 sm:grid-cols-3">
          {[
            ["Minhas ofertas", "Acompanhe propostas enviadas."],
            ["Favoritos", "Guarde imóveis interessantes."],
            ["Contratos", "Consulte documentos e assinaturas."],
          ].map(([titulo, texto]) => <section key={titulo} className="rounded-2xl border border-stone-200 bg-white p-6"><h2 className="font-bold">{titulo}</h2><p className="mt-2 text-sm text-stone-600">{texto}</p><p className="mt-4 text-xs font-bold uppercase text-amber-700">Próxima etapa</p></section>)}
        </div>
      </main>
    </ProtectedRoute>
  );
}
