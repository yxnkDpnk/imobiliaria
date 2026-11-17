"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { useAuth } from "@/app/context/AuthContext";
import { api } from "@/app/lib/api";
import type { Imovel } from "@/app/types";

export default function NegociarImovel({ imovel }: { imovel: Imovel }) {
  const { sessao, carregando } = useAuth();
  const [favorito, setFavorito] = useState(false);
  const [tipo, setTipo] = useState((imovel.precoVenda ?? 0) > 0 ? "VENDA" : "ALUGUEL");
  const [valor, setValor] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const disponivel = imovel.status?.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toUpperCase() === "DISPONIVEL";

  useEffect(() => {
    if (sessao?.tipo !== "USER") return;
    let ativo = true;
    api<Imovel[]>("/favoritos").then((itens) => {
      if (ativo) setFavorito(itens.some((item) => item.id === imovel.id));
    }).catch((e) => { if (ativo) setErro(e.message); });
    return () => { ativo = false; };
  }, [sessao, imovel.id]);

  async function alternarFavorito() {
    setOcupado(true); setErro(""); setSucesso("");
    try {
      await api(`/favoritos/${imovel.id}`, { method: favorito ? "DELETE" : "POST" });
      setFavorito(!favorito);
      setSucesso(favorito ? "Imóvel removido dos favoritos." : "Imóvel adicionado aos favoritos.");
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível atualizar os favoritos."); }
    finally { setOcupado(false); }
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault(); setErro(""); setSucesso("");
    const numero = Number(valor);
    if (!Number.isFinite(numero) || numero <= 0 || numero > 9999999999999.99) return setErro("Informe um valor positivo válido.");
    if (!mensagem.trim()) return setErro("Escreva uma mensagem para acompanhar sua oferta.");
    setOcupado(true);
    try {
      await api("/ofertas", { method: "POST", body: JSON.stringify({ imovelId: imovel.id, tipo, valorProposto: numero, mensagem: mensagem.trim() }) });
      setSucesso("Oferta enviada. Acompanhe em Minha área → Minhas ofertas."); setValor(""); setMensagem("");
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível enviar a oferta."); }
    finally { setOcupado(false); }
  }

  if (carregando) return null;
  if (!sessao) return <section className="mt-6 rounded-2xl border border-stone-200 bg-white p-6"><h2 className="text-xl font-bold">Gostou deste imóvel?</h2><Link href="/login" className="mt-3 inline-block font-bold text-emerald-800 underline">Entre para favoritar ou fazer uma oferta</Link></section>;
  if (sessao.tipo !== "USER") return null;
  return <section className="mt-6 rounded-2xl border border-stone-200 bg-white p-6">
    <button onClick={alternarFavorito} disabled={ocupado} className="w-full rounded-lg border border-emerald-800 p-3 font-bold text-emerald-900 disabled:opacity-50">{favorito ? "♥ Remover dos favoritos" : "♡ Salvar nos favoritos"}</button>
    {erro && <p role="alert" className="mt-4 rounded-lg bg-red-50 p-3 text-sm text-red-800">{erro}</p>}
    {sucesso && <p role="status" className="mt-4 rounded-lg bg-emerald-50 p-3 text-sm text-emerald-900">{sucesso} <Link href="/cliente" className="underline">Minha área</Link></p>}
    {disponivel ? <form onSubmit={enviar} noValidate className="mt-6 space-y-4">
      <h2 className="text-xl font-bold">Fazer oferta</h2>
      <label className="block text-sm font-semibold">Tipo de oferta<select value={tipo} onChange={(e) => setTipo(e.target.value)} className="mt-2 w-full rounded-lg border border-stone-300 p-3">{(imovel.precoVenda ?? 0) > 0 && <option value="VENDA">Compra</option>}{(imovel.precoAluguel ?? 0) > 0 && <option value="ALUGUEL">Aluguel</option>}</select></label>
      <label className="block text-sm font-semibold">Valor proposto (R$)<input type="number" min="0.01" step="0.01" value={valor} onChange={(e) => setValor(e.target.value)} className="mt-2 w-full rounded-lg border border-stone-300 p-3" /></label>
      <label className="block text-sm font-semibold">Mensagem<textarea rows={3} maxLength={3000} value={mensagem} onChange={(e) => setMensagem(e.target.value)} className="mt-2 w-full rounded-lg border border-stone-300 p-3" /></label>
      <button disabled={ocupado} className="w-full rounded-lg bg-emerald-900 p-3 font-bold text-white disabled:opacity-50">{ocupado ? "Aguarde…" : "Enviar oferta"}</button>
    </form> : <p className="mt-4 text-sm text-stone-600">Este imóvel está {imovel.status?.toLowerCase()} e não recebe novas ofertas.</p>}
  </section>;
}
