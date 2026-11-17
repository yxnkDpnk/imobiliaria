"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useState } from "react";
import ProtectedRoute from "@/app/components/ProtectedRoute";
import { useAuth } from "@/app/context/AuthContext";
import { api, baixarArquivo } from "@/app/lib/api";
import type { Contrato, Imovel, Oferta } from "@/app/types";

const moeda = (valor: number) => valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const data = (valor: string) => new Date(valor).toLocaleString("pt-BR");
const situacao = (valor: string) => valor.replaceAll("_", " ").toLowerCase();
const botao = "rounded-lg border border-stone-300 px-4 py-2 text-sm font-semibold disabled:opacity-50";

function AreaCliente() {
  const { sessao } = useAuth();
  const [aba, setAba] = useState("ofertas");
  const [ofertas, setOfertas] = useState<Oferta[]>([]);
  const [contratos, setContratos] = useState<Contrato[]>([]);
  const [favoritos, setFavoritos] = useState<Imovel[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [feedback, setFeedback] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [assinando, setAssinando] = useState<Contrato | null>(null);
  const [nome, setNome] = useState("");
  const [aceite, setAceite] = useState(false);
  const [filtro, setFiltro] = useState("");

  const carregar = useCallback(async () => {
    setCarregando(true); setErro("");
    try {
      const [o, c, f] = await Promise.all([api<Oferta[]>("/ofertas"), api<Contrato[]>("/contratos"), api<Imovel[]>("/favoritos")]);
      setOfertas(o); setContratos(c); setFavoritos(f);
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível carregar sua área."); }
    finally { setCarregando(false); }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => { void carregar(); }, 0);
    return () => window.clearTimeout(timer);
  }, [carregar]);

  async function acao(caminho: string, method = "PATCH", body?: object) {
    setOcupado(true); setErro(""); setFeedback("");
    try {
      await api(caminho, { method, body: body ? JSON.stringify(body) : undefined });
      setFeedback("Operação concluída."); setAssinando(null);
      await carregar();
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível concluir a operação."); }
    finally { setOcupado(false); }
  }

  async function baixar(contrato: Contrato) {
    setOcupado(true); setErro("");
    try { await baixarArquivo(`/contratos/${contrato.id}/pdf`, `contrato-${contrato.id}.pdf`); }
    catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível baixar o contrato."); }
    finally { setOcupado(false); }
  }

  function assinar(event: FormEvent) {
    event.preventDefault();
    if (!nome.trim() || !aceite) return setErro("Informe seu nome completo e confirme que leu e aceita o contrato.");
    if (assinando) void acao(`/contratos/${assinando.id}/assinar`, "PATCH", { nomeAssinatura: nome.trim(), aceite });
  }

  return <main className="mx-auto max-w-6xl px-5 py-10">
    <p className="text-sm font-bold uppercase tracking-widest text-emerald-800">Área do cliente</p>
    <div className="mt-2 flex flex-wrap items-center justify-between gap-4"><h1 className="text-3xl font-extrabold">Olá, {sessao?.nome}</h1><button className={botao} disabled={carregando || ocupado} onClick={carregar}>Atualizar</button></div>
    <p className="mt-3 text-stone-600">Acompanhe suas propostas, seus imóveis favoritos e os contratos recebidos.</p>
    <nav aria-label="Minha área" className="my-7 flex flex-wrap gap-2">
      {[["ofertas", "Minhas ofertas"], ["favoritos", "Favoritos"], ["contratos", "Meus contratos"]].map(([id, label]) => <button key={id} onClick={() => { setAba(id); setFiltro(""); }} aria-current={aba === id ? "page" : undefined} className={`${botao} ${aba === id ? "bg-emerald-900 text-white" : "bg-white"}`}>{label}</button>)}
    </nav>
    {feedback && <p role="status" className="mb-5 rounded-xl bg-emerald-50 p-4 text-emerald-900">{feedback}</p>}
    {erro && <div role="alert" className="mb-5 rounded-xl bg-red-50 p-4 text-red-800">{erro} <button className="ml-3 underline" onClick={carregar}>Tentar novamente</button></div>}
    {carregando ? <p role="status">Carregando dados…</p> : <>
      {aba === "ofertas" && <section>
        <label className="mb-5 block text-sm font-semibold">Situação da oferta<select value={filtro} onChange={(e) => setFiltro(e.target.value)} className="ml-3 rounded-lg border border-stone-300 p-2"><option value="">Todas</option>{["PENDENTE", "ACEITA", "EM_ESPERA", "RECUSADA", "CANCELADA"].map((s) => <option key={s} value={s}>{situacao(s)}</option>)}</select></label>
        {ofertas.filter((o) => !filtro || o.status === filtro).length === 0 && <p className="rounded-xl bg-stone-50 p-6">Nenhuma oferta encontrada. <Link href="/" className="text-emerald-800 underline">Escolher um imóvel</Link></p>}
        <div className="space-y-4">{ofertas.filter((o) => !filtro || o.status === filtro).map((oferta) => <article key={oferta.id} className="rounded-2xl border border-stone-200 p-6">
          <div className="flex flex-wrap items-start justify-between gap-3"><Link className="text-lg font-bold text-emerald-900 underline" href={`/imoveis/${oferta.imovel.id}`}>{oferta.imovel.titulo} · #{oferta.imovel.id}</Link><span className="rounded-full bg-stone-100 px-3 py-1 text-sm font-bold capitalize">{situacao(oferta.status)}</span></div>
          <p className="mt-3 font-semibold">{oferta.tipo === "VENDA" ? "Compra" : "Aluguel"} · {moeda(oferta.valorProposto)}</p><p className="mt-2 whitespace-pre-wrap text-stone-600">{oferta.mensagem}</p><p className="mt-2 text-xs text-stone-500">Enviada em {data(oferta.createdAt)}</p>
          {["PENDENTE", "EM_ESPERA"].includes(oferta.status) && <button disabled={ocupado} className={`${botao} mt-4`} onClick={() => { if (confirm("Cancelar esta oferta?")) void acao(`/ofertas/${oferta.id}/cancelar`); }}>Cancelar oferta</button>}
        </article>)}</div>
      </section>}
      {aba === "favoritos" && <section className="grid gap-4 sm:grid-cols-2">
        {favoritos.length === 0 && <p className="rounded-xl bg-stone-50 p-6">Você ainda não salvou imóveis. <Link href="/" className="text-emerald-800 underline">Ver vitrine</Link></p>}
        {favoritos.map((imovel) => <article key={imovel.id} className="rounded-2xl border border-stone-200 p-6"><h2 className="text-lg font-bold">{imovel.titulo}</h2><p className="mt-2 text-sm text-stone-600">{imovel.bairro?.nome} · {imovel.bairro?.cidade} · {imovel.status}</p><div className="mt-4 flex flex-wrap gap-3"><Link href={`/imoveis/${imovel.id}`} className={botao}>Ver imóvel</Link><button disabled={ocupado} className={botao} onClick={() => acao(`/favoritos/${imovel.id}`, "DELETE")}>Remover favorito</button></div></article>)}
      </section>}
      {aba === "contratos" && <section className="space-y-4">
        {contratos.length === 0 && <p className="rounded-xl bg-stone-50 p-6">Você ainda não recebeu contratos. Eles aparecem aqui depois da aprovação e do envio pelo administrador.</p>}
        {contratos.map((contrato) => <article key={contrato.id} className="rounded-2xl border border-stone-200 p-6">
          <div className="flex flex-wrap justify-between gap-3"><h2 className="text-lg font-bold">Contrato #{contrato.id} · {contrato.oferta.imovel.titulo}</h2><span className="rounded-full bg-stone-100 px-3 py-1 text-sm capitalize">{situacao(contrato.status)}</span></div>
          <p className="mt-3">{contrato.oferta.tipo === "VENDA" ? "Compra" : "Aluguel"} · {moeda(contrato.oferta.valorProposto)}</p><p className="mt-2 whitespace-pre-wrap text-sm text-stone-600">{contrato.observacoes}</p>
          {contrato.dataAssinatura && <p className="mt-3 text-sm text-emerald-800">Assinado por {contrato.nomeAssinatura} em {data(contrato.dataAssinatura)}.</p>}
          <div className="mt-4 flex flex-wrap gap-3"><button disabled={ocupado} className={botao} onClick={() => baixar(contrato)}>Baixar PDF</button>{contrato.status === "AGUARDANDO_ASSINATURA" && contrato.dataEnvio && <button disabled={ocupado} className={`${botao} bg-emerald-900 text-white`} onClick={() => { setAssinando(contrato); setNome(sessao?.nome ?? ""); setAceite(false); setErro(""); }}>Assinar contrato</button>}</div>
        </article>)}
      </section>}
    </>}
    {assinando && <section role="dialog" aria-modal="true" aria-labelledby="titulo-assinatura" className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto bg-black/50 p-5"><form onSubmit={assinar} className="w-full max-w-lg rounded-2xl bg-white p-7">
      <h2 id="titulo-assinatura" className="text-xl font-bold">Assinar contrato #{assinando.id}</h2><p className="mt-3 text-sm text-stone-600">Leia o PDF antes de confirmar. O aceite registrará seu nome e a data da assinatura.</p>
      <button type="button" className={`${botao} mt-4`} onClick={() => baixar(assinando)} disabled={ocupado}>Baixar PDF para leitura</button>
      <label className="mt-5 block text-sm font-semibold">Nome completo<input value={nome} onChange={(e) => setNome(e.target.value)} maxLength={200} className="mt-2 w-full rounded-lg border border-stone-300 p-3" /></label>
      <label className="mt-5 flex items-start gap-3 text-sm"><input className="mt-1" type="checkbox" checked={aceite} onChange={(e) => setAceite(e.target.checked)} />Li o contrato e concordo com os termos.</label>
      {erro && <p role="alert" className="mt-3 text-sm text-red-800">{erro}</p>}
      <div className="mt-6 flex gap-3"><button disabled={ocupado} className={`${botao} bg-emerald-900 text-white`}>{ocupado ? "Assinando…" : "Confirmar assinatura"}</button><button type="button" className={botao} disabled={ocupado} onClick={() => setAssinando(null)}>Voltar</button></div>
    </form></section>}
  </main>;
}

export default function ClientePage() { return <ProtectedRoute perfil="USER"><AreaCliente /></ProtectedRoute>; }
