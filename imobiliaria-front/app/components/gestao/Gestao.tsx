"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import Link from "next/link";
import ProtectedRoute from "@/app/components/ProtectedRoute";
import { useAuth } from "@/app/context/AuthContext";
import { api, baixarArquivo } from "@/app/lib/api";
import type { Contrato, Oferta, Painel } from "@/app/types";
import Cadastros from "./Cadastros";

const botao = "cursor-pointer rounded-lg bg-emerald-900 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-800 disabled:cursor-wait disabled:opacity-50";
const secundario = "cursor-pointer rounded-lg border border-stone-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-stone-100 disabled:opacity-50";
const rotulos: Record<string, string> = { PENDENTE: "Pendente", ACEITA: "Aceita", EM_ESPERA: "Em espera", RECUSADA: "Recusada", CANCELADA: "Cancelada", EM_APROVACAO: "Em aprovação", AGUARDANDO_ASSINATURA: "Aguardando assinatura", ASSINADO: "Assinado", RECUSADO: "Recusado", CANCELADO: "Cancelado", VENDA: "Venda", ALUGUEL: "Aluguel" };
const moeda = (valor: number) => valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const data = (valor?: string | null) => valor ? new Date(valor).toLocaleString("pt-BR") : "—";

function Grafico({ titulo, dados }: { titulo: string; dados: { nome: string; total: number }[] }) {
  const maior = Math.max(1, ...dados.map((item) => item.total));
  return <section className="rounded-2xl border border-stone-200 bg-white p-6"><h2 className="mb-5 text-lg font-bold">{titulo}</h2>{dados.length === 0 && <p className="text-sm text-stone-500">Sem resultados.</p>}<ul className="space-y-4">{dados.map((item) => <li key={item.nome}><div className="mb-1 flex justify-between gap-4 text-sm"><span>{rotulos[item.nome] ?? item.nome}</span><strong>{item.total}</strong></div><div className="h-3 overflow-hidden rounded-full bg-stone-100"><div className="h-full rounded-full bg-emerald-700" style={{ width: `${100 * item.total / maior}%` }} /></div></li>)}</ul></section>;
}

function ConteudoGestao({ admin }: { admin: boolean }) {
  const { sessao } = useAuth();
  const [aba, setAba] = useState(admin ? "painel" : "ofertas");
  const [painel, setPainel] = useState<Painel | null>(null);
  const [ofertas, setOfertas] = useState<Oferta[]>([]);
  const [contratos, setContratos] = useState<Contrato[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [filtroOferta, setFiltroOferta] = useState("");
  const [filtroContrato, setFiltroContrato] = useState("");
  const [preparar, setPreparar] = useState<number | null>(null);
  const [observacoes, setObservacoes] = useState("");

  const carregar = useCallback(async () => {
    setCarregando(true);
    setErro("");
    try {
      const [indicadores, propostas, documentos] = await Promise.all([api<Painel>("/painel"), api<Oferta[]>("/ofertas"), api<Contrato[]>("/contratos")]);
      setPainel(indicadores); setOfertas(propostas); setContratos(documentos);
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível buscar os dados."); }
    finally { setCarregando(false); }
  }, []);
  useEffect(() => { const timer = window.setTimeout(() => void carregar(), 0); return () => window.clearTimeout(timer); }, [carregar]);

  async function executar(caminho: string, mensagem: string, corpo?: unknown) {
    setOcupado(true); setAviso("");
    try {
      await api(caminho, { method: corpo ? "POST" : "PATCH", ...(corpo ? { body: JSON.stringify(corpo) } : {}) });
      setAviso(mensagem); setPreparar(null); setObservacoes(""); await carregar();
    } catch (e) { setAviso(e instanceof Error ? e.message : "Não foi possível concluir a operação."); }
    finally { setOcupado(false); }
  }

  async function baixar(id: number) {
    setOcupado(true); setAviso("");
    try { await baixarArquivo(`/contratos/${id}/pdf`, `contrato-${id}.pdf`); }
    catch (e) { setAviso(e instanceof Error ? e.message : "Não foi possível baixar o PDF."); }
    finally { setOcupado(false); }
  }

  function prepararContrato(evento: FormEvent) {
    evento.preventDefault();
    if (preparar) void executar("/contratos", "Contrato preparado e encaminhado para aprovação do administrador.", { ofertaId: preparar, observacoes });
  }

  const propostas = ofertas.filter((item) => !filtroOferta || item.status === filtroOferta);
  const documentos = contratos.filter((item) => !filtroContrato || item.status === filtroContrato);

  return <main className="mx-auto max-w-6xl px-5 py-10">
    <div className="flex flex-wrap items-start justify-between gap-4"><div><p className="text-sm font-bold uppercase tracking-widest text-emerald-800">{admin ? "Administração" : "Área do corretor"}</p><h1 className="mt-2 text-3xl font-extrabold">Olá, {sessao?.nome}</h1><p className="mt-2 text-stone-600">{admin ? "Acompanhe os resultados, os cadastros e a aprovação dos contratos." : "Analise propostas e prepare os contratos para aprovação."}</p></div><button onClick={() => void carregar()} disabled={carregando || ocupado} className={secundario}>Atualizar dados</button></div>
    <nav aria-label="Seções da gestão" className="my-7 flex flex-wrap gap-2">{[["painel", "Painel"], ["ofertas", "Ofertas"], ["contratos", "Contratos"], ...(admin ? [["cadastros", "Cadastros"]] : [])].map(([chave, titulo]) => <button key={chave} onClick={() => { setAba(chave); setAviso(""); }} aria-current={aba === chave ? "page" : undefined} className={aba === chave ? botao : secundario}>{titulo}</button>)}</nav>
    {aviso && <p role="status" className="mb-5 rounded-xl border border-emerald-200 bg-emerald-50 p-4 text-emerald-950">{aviso}</p>}
    {aba === "cadastros" && admin ? <Cadastros /> : <>
      {carregando && <p role="status" className="rounded-xl bg-stone-100 p-6">Carregando dados…</p>}
      {!carregando && erro && <div role="alert" className="rounded-xl bg-red-50 p-5 text-red-900"><p>{erro}</p><button className={`${secundario} mt-3`} onClick={() => void carregar()}>Tentar novamente</button></div>}
      {!carregando && !erro && <>
        {aba === "painel" && painel && <><div className="mb-7 grid gap-4 sm:grid-cols-3">{[["Imóveis disponíveis", painel.imoveisDisponiveis], ["Ofertas pendentes", painel.ofertasPendentes], ["Contratos aguardando assinatura", painel.contratosAguardandoAssinatura]].map(([nome, total]) => <section key={nome} className="rounded-2xl bg-emerald-950 p-6 text-white"><p className="text-sm text-emerald-100">{nome}</p><p className="mt-3 text-4xl font-extrabold">{total}</p></section>)}</div><div className="grid items-start gap-5 lg:grid-cols-3"><Grafico titulo="Imóveis por bairro" dados={painel.imoveisPorBairro} /><Grafico titulo="Ofertas por situação" dados={painel.ofertasPorSituacao} /><Grafico titulo="Imóveis por finalidade" dados={painel.imoveisPorFinalidade} /></div></>}
        {aba === "ofertas" && <section><div className="mb-5 flex flex-wrap items-center justify-between gap-3"><h2 className="text-xl font-bold">Ofertas recebidas</h2><label className="text-sm font-semibold">Situação <select className="ml-2 rounded-lg border border-stone-300 p-2" value={filtroOferta} onChange={(e) => setFiltroOferta(e.target.value)}><option value="">Todas</option>{["PENDENTE", "ACEITA", "EM_ESPERA", "RECUSADA", "CANCELADA"].map((status) => <option key={status} value={status}>{rotulos[status]}</option>)}</select></label></div>
          {propostas.length === 0 && <p className="rounded-xl bg-stone-100 p-6">Nenhuma oferta encontrada.</p>}
          <div className="space-y-4">{propostas.map((oferta) => <article key={oferta.id} className="rounded-2xl border border-stone-200 bg-white p-6"><div className="flex flex-wrap justify-between gap-3"><div><p className="text-sm text-stone-500">Oferta #{oferta.id} · {data(oferta.createdAt)}</p><h3 className="mt-1 text-xl font-bold"><Link className="hover:underline" href={`/imoveis/${oferta.imovel.id}`}>{oferta.imovel.titulo}</Link></h3><p className="mt-1 text-sm text-stone-600">{oferta.usuario.nome} · {oferta.usuario.email}</p></div><div className="text-left sm:text-right"><p className="font-bold text-emerald-900">{rotulos[oferta.status] ?? oferta.status}</p><p className="mt-1 text-lg font-bold">{moeda(oferta.valorProposto)}</p><p className="text-sm text-stone-500">{rotulos[oferta.tipo]}</p></div></div>{oferta.mensagem && <p className="mt-4 whitespace-pre-wrap text-stone-600">{oferta.mensagem}</p>}<div className="mt-5 flex flex-wrap gap-2">{oferta.status === "PENDENTE" && <><button disabled={ocupado} className={botao} onClick={() => { if (confirm("Aceitar esta oferta? O imóvel será reservado e as outras propostas ficarão em espera.")) void executar(`/ofertas/${oferta.id}/aceitar`, "Oferta aceita. O imóvel foi reservado."); }}>Aceitar oferta</button><button disabled={ocupado} className={secundario} onClick={() => { if (confirm("Recusar esta oferta?")) void executar(`/ofertas/${oferta.id}/recusar`, "Oferta recusada."); }}>Recusar</button></>}{oferta.status === "ACEITA" && !contratos.some((contrato) => contrato.oferta.id === oferta.id && !["CANCELADO", "RECUSADO"].includes(contrato.status)) && <button disabled={ocupado} className={botao} onClick={() => { setPreparar(oferta.id); setObservacoes(""); }}>Preparar contrato</button>}</div>{preparar === oferta.id && <form onSubmit={prepararContrato} className="mt-5 space-y-3 rounded-xl bg-stone-50 p-4"><label className="block text-sm font-semibold">Observações do contrato<textarea className="mt-2 w-full rounded-lg border border-stone-300 bg-white p-3 font-normal" rows={3} maxLength={3000} value={observacoes} onChange={(e) => setObservacoes(e.target.value)} /></label><p className="text-sm text-stone-600">O PDF será gerado com os dados da oferta e enviado ao administrador para análise.</p><div className="flex gap-2"><button disabled={ocupado} className={botao}>{ocupado ? "Preparando…" : "Gerar contrato"}</button><button type="button" className={secundario} onClick={() => setPreparar(null)}>Voltar</button></div></form>}</article>)}</div>
        </section>}
        {aba === "contratos" && <section><div className="mb-5 flex flex-wrap items-center justify-between gap-3"><h2 className="text-xl font-bold">Contratos</h2><label className="text-sm font-semibold">Situação <select className="ml-2 rounded-lg border border-stone-300 p-2" value={filtroContrato} onChange={(e) => setFiltroContrato(e.target.value)}><option value="">Todas</option>{["EM_APROVACAO", "AGUARDANDO_ASSINATURA", "ASSINADO", "RECUSADO", "CANCELADO"].map((status) => <option key={status} value={status}>{rotulos[status]}</option>)}</select></label></div>{documentos.length === 0 && <p className="rounded-xl bg-stone-100 p-6">Nenhum contrato encontrado.</p>}<div className="space-y-4">{documentos.map((contrato) => <article key={contrato.id} className="rounded-2xl border border-stone-200 bg-white p-6"><div className="flex flex-wrap justify-between gap-3"><div><p className="text-sm text-stone-500">Contrato #{contrato.id} · Oferta #{contrato.oferta.id}</p><h3 className="mt-1 text-xl font-bold">{contrato.oferta.imovel.titulo}</h3><p className="mt-1 text-sm text-stone-600">{contrato.oferta.usuario.nome} · {moeda(contrato.oferta.valorProposto)}</p></div><strong className="text-emerald-900">{rotulos[contrato.status] ?? contrato.status}</strong></div>{contrato.observacoes && <p className="mt-4 whitespace-pre-wrap text-sm text-stone-600">{contrato.observacoes}</p>}<dl className="mt-4 grid gap-2 text-sm sm:grid-cols-3"><div><dt className="text-stone-500">Aprovação</dt><dd>{data(contrato.dataAprovacao)}</dd></div><div><dt className="text-stone-500">Envio ao cliente</dt><dd>{data(contrato.dataEnvio)}</dd></div><div><dt className="text-stone-500">Assinatura</dt><dd>{data(contrato.dataAssinatura)}{contrato.nomeAssinatura && ` · ${contrato.nomeAssinatura}`}</dd></div></dl>{contrato.dataEnvio && <p className="mt-3 text-sm text-stone-600">Notificação por email: {contrato.notificacaoStatus === "ENVIADO" ? "enviada" : contrato.notificacaoStatus === "FALHA" ? "falha no envio; contrato disponível na área do cliente" : "pendente de configuração de SMTP; contrato disponível na área do cliente"}.</p>}<div className="mt-5 flex flex-wrap gap-2"><button disabled={ocupado} className={secundario} onClick={() => void baixar(contrato.id)}>Baixar PDF</button>{admin && contrato.status === "EM_APROVACAO" && <><button disabled={ocupado} className={botao} onClick={() => void executar(`/contratos/${contrato.id}/aprovar`, "Contrato aprovado. Envie-o para o cliente na próxima ação.")}>Aprovar contrato</button><button disabled={ocupado} className={secundario} onClick={() => { if (confirm("Recusar este contrato?")) void executar(`/contratos/${contrato.id}/recusar`, "Contrato recusado. O imóvel foi liberado para novas ofertas."); }}>Recusar contrato</button></>}{admin && contrato.dataAprovacao && !contrato.dataEnvio && !["CANCELADO", "RECUSADO", "ASSINADO"].includes(contrato.status) && <button disabled={ocupado} className={botao} onClick={() => void executar(`/contratos/${contrato.id}/enviar`, "Contrato disponibilizado para o cliente. Confira o estado da notificação por email abaixo.")}>Enviar ao cliente</button>}{admin && !["CANCELADO", "RECUSADO"].includes(contrato.status) && <button disabled={ocupado} className={secundario} onClick={() => { if (confirm("Cancelar o contrato? O imóvel ficará disponível novamente.")) void executar(`/contratos/${contrato.id}/cancelar`, "Contrato cancelado e imóvel disponível novamente."); }}>Cancelar contrato</button>}</div></article>)}</div></section>}
      </>}
    </>}
  </main>;
}

export default function Gestao({ admin = false }: { admin?: boolean }) {
  return <ProtectedRoute perfil={admin ? "ADMIN" : "CORRETOR"}><ConteudoGestao admin={admin} /></ProtectedRoute>;
}
