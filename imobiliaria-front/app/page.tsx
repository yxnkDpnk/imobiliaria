"use client";

import { useEffect, useState } from "react";

type Imovel = {
  id: number;
  titulo: string | null;
  descricao: string | null;
  status: string | null;
  finalidade: string | null;
  precoVenda: number | null;
  precoAluguel: number | null;
  dormitorios: number | null;
  bairro: { nome: string } | null;
  tipoImovel: { nome: string } | null;
};

function moeda(valor: number) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export default function Home() {
  const [imoveis, setImoveis] = useState<Imovel[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    async function carregar() {
      setCarregando(true);
      setErro("");
      try {
        // O Next encaminha esta chamada para o Spring (next.config.ts).
        const resposta = await fetch("/api/imoveis", {
          signal: controller.signal,
          cache: "no-store",
        });
        if (!resposta.ok) throw new Error("Falha ao consultar imóveis");
        const dados: Imovel[] = await resposta.json();
        if (!Array.isArray(dados)) throw new Error("Resposta inesperada");
        setImoveis(dados.filter((imovel) =>
          imovel.status?.normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim().toUpperCase() === "DISPONIVEL"
        ));
      } catch {
        if (!controller.signal.aborted) {
          setErro("Não foi possível carregar os imóveis. Tente novamente em instantes.");
        }
      } finally {
        if (!controller.signal.aborted) setCarregando(false);
      }
    }
    carregar();
    return () => controller.abort();
  }, [tentativa]);

  return (
    <div className="min-h-screen bg-stone-50 text-stone-900">
      <header className="border-b border-stone-200 bg-white px-6 py-5">
        <div className="mx-auto max-w-5xl text-xl font-bold text-emerald-900">Imobiliária</div>
      </header>
      <main className="mx-auto max-w-5xl px-6 py-12">
        <p className="mb-3 text-sm font-semibold uppercase tracking-widest text-emerald-800">Encontre seu lugar</p>
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Imóveis disponíveis</h1>
        <p className="mt-3 mb-8 text-stone-600">Conheça as opções para o seu próximo endereço.</p>

        {carregando && <p role="status">Carregando imóveis…</p>}
        {!carregando && erro && (
          <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-6">
            <p>{erro}</p>
            <button onClick={() => setTentativa((valor) => valor + 1)} className="mt-4 cursor-pointer rounded-lg bg-emerald-900 px-4 py-2 text-white focus-visible:outline-2 focus-visible:outline-offset-4">Tentar novamente</button>
          </div>
        )}
        {!carregando && !erro && imoveis.length === 0 && <p role="status">Nenhum imóvel disponível no momento.</p>}
        {!carregando && !erro && imoveis.length > 0 && (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {imoveis.map((imovel) => (
              <article key={imovel.id} className="overflow-hidden rounded-2xl border border-stone-200 bg-white">
                <div className="flex h-36 items-center justify-center bg-stone-100 text-sm text-stone-500">Foto não disponível</div>
                <div className="p-6">
                  <p className="text-xs font-semibold uppercase tracking-wide text-emerald-800">{imovel.tipoImovel?.nome ?? "Imóvel"} · #{imovel.id}</p>
                  <h2 className="mt-2 text-xl font-semibold">{imovel.titulo ?? "Imóvel sem título"}</h2>
                  <p className="mt-1 text-sm text-stone-600">{imovel.bairro?.nome ?? "Bairro não informado"}</p>
                  <p className="mt-4 text-sm text-stone-600">{imovel.descricao}</p>
                  <dl className="mt-5 space-y-2 border-t border-stone-100 pt-4 text-sm">
                    {imovel.precoVenda != null && <div><dt className="text-stone-500">Venda</dt><dd className="text-lg font-semibold">{moeda(imovel.precoVenda)}</dd></div>}
                    {imovel.precoAluguel != null && <div><dt className="text-stone-500">Aluguel / mês</dt><dd className="text-lg font-semibold">{moeda(imovel.precoAluguel)}</dd></div>}
                    {imovel.precoVenda == null && imovel.precoAluguel == null && <div><dt>Valor</dt><dd>Sob consulta</dd></div>}
                    {imovel.dormitorios != null && <div><dt className="inline">Dormitórios: </dt><dd className="inline">{imovel.dormitorios}</dd></div>}
                  </dl>
                </div>
              </article>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
