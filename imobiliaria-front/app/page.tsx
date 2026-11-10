"use client";

import Image from "next/image";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { api } from "@/app/lib/api";
import type { Foto, Imovel } from "@/app/types";

const POR_PAGINA = 6;

function moeda(valor: number) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function normalizar(valor: string | null | undefined) {
  return valor?.normalize("NFD").replace(/[\u0300-\u036f]/g, "").trim().toUpperCase() ?? "";
}

function valorPrincipal(imovel: Imovel) {
  return imovel.precoVenda ?? imovel.precoAluguel ?? 0;
}

function valorDoFiltro(imovel: Imovel, finalidade: string) {
  if (finalidade === "VENDA") return imovel.precoVenda ?? 0;
  if (finalidade === "ALUGUEL") return imovel.precoAluguel ?? 0;
  return valorPrincipal(imovel);
}

export default function Home() {
  const [imoveis, setImoveis] = useState<Imovel[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [bairro, setBairro] = useState("");
  const [finalidade, setFinalidade] = useState("");
  const [precoMin, setPrecoMin] = useState("");
  const [precoMax, setPrecoMax] = useState("");
  const [ordem, setOrdem] = useState("preco-asc");
  const [pagina, setPagina] = useState(1);

  useEffect(() => {
    let ativo = true;
    async function carregar() {
      setCarregando(true);
      setErro("");
      try {
        const dados = await api<Imovel[]>("/imoveis");
        const disponiveis = dados.filter((imovel) => normalizar(imovel.status) === "DISPONIVEL");
        const comFotos = await Promise.all(disponiveis.map(async (imovel) => {
          try {
            const fotos = await api<Foto[]>(`/fotos/imovel/${imovel.id}`);
            return { ...imovel, fotos };
          } catch {
            return { ...imovel, fotos: [] };
          }
        }));
        if (ativo) setImoveis(comFotos);
      } catch (e) {
        if (ativo) setErro(e instanceof Error ? e.message : "Não foi possível carregar os imóveis.");
      } finally {
        if (ativo) setCarregando(false);
      }
    }
    carregar();
    return () => { ativo = false; };
  }, [tentativa]);

  const bairros = useMemo(() => [...new Set(imoveis.map((i) => i.bairro?.nome).filter(Boolean) as string[])].sort(), [imoveis]);
  const filtrados = useMemo(() => {
    const minimo = precoMin ? Number(precoMin) : 0;
    const maximo = precoMax ? Number(precoMax) : Number.POSITIVE_INFINITY;
    return imoveis
      .filter((imovel) => !bairro || imovel.bairro?.nome === bairro)
      .filter((imovel) => finalidade !== "VENDA" || imovel.precoVenda != null)
      .filter((imovel) => finalidade !== "ALUGUEL" || imovel.precoAluguel != null)
      .filter((imovel) => valorDoFiltro(imovel, finalidade) >= minimo && valorDoFiltro(imovel, finalidade) <= maximo)
      .sort((a, b) => ordem === "preco-desc"
        ? valorDoFiltro(b, finalidade) - valorDoFiltro(a, finalidade)
        : valorDoFiltro(a, finalidade) - valorDoFiltro(b, finalidade));
  }, [bairro, finalidade, imoveis, ordem, precoMax, precoMin]);

  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA));
  const paginaAtual = Math.min(pagina, totalPaginas);
  const visiveis = filtrados.slice((paginaAtual - 1) * POR_PAGINA, paginaAtual * POR_PAGINA);

  return (
    <main className="min-h-screen bg-stone-50 text-stone-900">
      <section className="border-b border-emerald-950/10 bg-emerald-950 px-5 py-14 text-white">
        <div className="mx-auto max-w-6xl">
          <p className="mb-3 text-sm font-bold uppercase tracking-[0.2em] text-emerald-200">Encontre seu lugar</p>
          <h1 className="max-w-2xl text-4xl font-extrabold tracking-tight sm:text-5xl">Imóveis disponíveis para a sua próxima história.</h1>
          <p className="mt-4 max-w-2xl text-emerald-100">Filtre por bairro, finalidade e preço. Abra o imóvel para ver todos os detalhes.</p>
        </div>
      </section>

      <div className="mx-auto max-w-6xl px-5 py-10">
        <section aria-label="Filtros dos imóveis" className="mb-9 grid gap-4 rounded-2xl border border-stone-200 bg-white p-5 shadow-sm md:grid-cols-5">
          <label className="text-sm font-semibold">Bairro
            <select value={bairro} onChange={(e) => { setBairro(e.target.value); setPagina(1); }} className="mt-2 w-full rounded-lg border border-stone-300 bg-white p-2.5 font-normal">
              <option value="">Todos</option>
              {bairros.map((item) => <option key={item}>{item}</option>)}
            </select>
          </label>
          <label className="text-sm font-semibold">Finalidade
            <select value={finalidade} onChange={(e) => { setFinalidade(e.target.value); setPagina(1); }} className="mt-2 w-full rounded-lg border border-stone-300 bg-white p-2.5 font-normal">
              <option value="">Todas</option><option value="VENDA">Venda</option><option value="ALUGUEL">Aluguel</option>
            </select>
          </label>
          <label className="text-sm font-semibold">Preço mínimo
            <input value={precoMin} onChange={(e) => { setPrecoMin(e.target.value); setPagina(1); }} type="number" min="0" placeholder="R$ 0" className="mt-2 w-full rounded-lg border border-stone-300 p-2.5 font-normal" />
          </label>
          <label className="text-sm font-semibold">Preço máximo
            <input value={precoMax} onChange={(e) => { setPrecoMax(e.target.value); setPagina(1); }} type="number" min="0" placeholder="Sem limite" className="mt-2 w-full rounded-lg border border-stone-300 p-2.5 font-normal" />
          </label>
          <label className="text-sm font-semibold">Ordenar
            <select value={ordem} onChange={(e) => { setOrdem(e.target.value); setPagina(1); }} className="mt-2 w-full rounded-lg border border-stone-300 bg-white p-2.5 font-normal">
              <option value="preco-asc">Menor preço</option><option value="preco-desc">Maior preço</option>
            </select>
          </label>
        </section>

        {carregando && <p role="status" className="rounded-xl bg-white p-8 text-center">Carregando imóveis…</p>}
        {!carregando && erro && (
          <div role="alert" className="rounded-xl border border-red-200 bg-red-50 p-6">
            <p>{erro}</p>
            <button onClick={() => setTentativa((valor) => valor + 1)} className="mt-4 cursor-pointer rounded-lg bg-emerald-900 px-4 py-2 text-white">Tentar novamente</button>
          </div>
        )}
        {!carregando && !erro && filtrados.length === 0 && <p role="status" className="rounded-xl bg-white p-8 text-center">Nenhum imóvel encontrado com esses filtros.</p>}

        {!carregando && !erro && visiveis.length > 0 && (
          <>
            <div className="mb-4 flex items-center justify-between text-sm text-stone-600"><span>{filtrados.length} imóvel(is)</span><span>Página {paginaAtual} de {totalPaginas}</span></div>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {visiveis.map((imovel) => {
                const foto = imovel.fotos?.find((item) => item.capa) ?? imovel.fotos?.[0];
                return (
                  <article key={imovel.id} className="overflow-hidden rounded-2xl border border-stone-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
                    <div className="relative h-52 bg-stone-100">
                      <Image src={foto ? `/api${foto.url}` : "/imovel-placeholder.svg"} alt={imovel.titulo ?? "Imóvel"} fill className="object-cover" sizes="(max-width: 768px) 100vw, 33vw" />
                    </div>
                    <div className="p-6">
                      <p className="text-xs font-bold uppercase tracking-wide text-emerald-800">{imovel.tipoImovel?.nome ?? "Imóvel"} · #{imovel.id}</p>
                      <h2 className="mt-2 text-xl font-bold">{imovel.titulo ?? "Imóvel sem título"}</h2>
                      <p className="mt-1 text-sm text-stone-600">{imovel.bairro?.nome ?? "Bairro não informado"} · {imovel.bairro?.cidade ?? "Cidade não informada"}</p>
                      <div className="mt-5 space-y-1 border-t border-stone-100 pt-4">
                        {imovel.precoVenda != null && <p><span className="text-sm text-stone-500">Venda </span><strong>{moeda(imovel.precoVenda)}</strong></p>}
                        {imovel.precoAluguel != null && <p><span className="text-sm text-stone-500">Aluguel </span><strong>{moeda(imovel.precoAluguel)}/mês</strong></p>}
                      </div>
                      <Link href={`/imoveis/${imovel.id}`} className="mt-5 block rounded-lg bg-emerald-900 px-4 py-2.5 text-center font-bold text-white hover:bg-emerald-800">Ver detalhes</Link>
                    </div>
                  </article>
                );
              })}
            </div>
            <nav aria-label="Paginação" className="mt-10 flex items-center justify-center gap-3">
              <button disabled={paginaAtual === 1} onClick={() => setPagina((p) => p - 1)} className="rounded-lg border border-stone-300 px-4 py-2 disabled:cursor-not-allowed disabled:opacity-40">Anterior</button>
              <span className="px-2 text-sm">{paginaAtual} / {totalPaginas}</span>
              <button disabled={paginaAtual === totalPaginas} onClick={() => setPagina((p) => p + 1)} className="rounded-lg border border-stone-300 px-4 py-2 disabled:cursor-not-allowed disabled:opacity-40">Próxima</button>
            </nav>
          </>
        )}
      </div>
    </main>
  );
}
