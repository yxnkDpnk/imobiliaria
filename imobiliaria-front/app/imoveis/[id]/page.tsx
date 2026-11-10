"use client";

import Image from "next/image";
import Link from "next/link";
import { FormEvent, Suspense, useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import { api, ApiError } from "@/app/lib/api";
import type { Foto, Imovel } from "@/app/types";

function moeda(valor: number | null) {
  return valor == null ? "Não informado" : valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function DetalheImovel() {
  const params = useParams<{ id: string }>();
  const [imovel, setImovel] = useState<Imovel | null>(null);
  const [fotos, setFotos] = useState<Foto[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [telefone, setTelefone] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [feedback, setFeedback] = useState("");
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    let ativo = true;
    Promise.all([
      api<Imovel>(`/imoveis/${params.id}`),
      api<Foto[]>(`/fotos/imovel/${params.id}`).catch(() => [] as Foto[]),
    ]).then(([dados, imagens]) => {
      if (!ativo) return;
      setImovel(dados);
      setFotos(imagens);
      const tipo = dados.tipoImovel?.nome ?? "imóvel";
      const bairro = dados.bairro?.nome ?? "bairro não informado";
      const valor = dados.precoVenda != null ? moeda(dados.precoVenda) : moeda(dados.precoAluguel);
      setMensagem(`Olá, tenho interesse no imóvel #${dados.id} (${tipo}), no bairro ${bairro}, anunciado por ${valor}. Gostaria de mais informações.`);
    }).catch((e) => {
      if (ativo) setErro(e instanceof ApiError && e.status === 404 ? "Imóvel não encontrado." : "Não foi possível carregar este imóvel.");
    }).finally(() => { if (ativo) setCarregando(false); });
    return () => { ativo = false; };
  }, [params.id]);

  const foto = useMemo(() => fotos.find((item) => item.capa) ?? fotos[0], [fotos]);

  async function enviarContato(evento: FormEvent) {
    evento.preventDefault();
    setFeedback("");
    if (!nome.trim() || !telefone.trim() || !email.trim() || !mensagem.trim()) return setFeedback("Preencha todos os campos.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return setFeedback("Informe um email válido.");
    setEnviando(true);
    try {
      const resposta = await api<string>(`/imoveis/sabermais/${params.id}`, {
        method: "POST",
        body: JSON.stringify({ nome, telefone, email, mensagem }),
      });
      setFeedback(resposta || "Interesse enviado com sucesso.");
    } catch (e) {
      setFeedback(e instanceof Error ? e.message : "Não foi possível enviar o contato.");
    } finally {
      setEnviando(false);
    }
  }

  if (carregando) return <main className="mx-auto max-w-6xl px-5 py-16" role="status">Carregando imóvel…</main>;
  if (erro || !imovel) return <main className="mx-auto max-w-6xl px-5 py-16"><h1 className="text-3xl font-bold">{erro || "Imóvel não encontrado."}</h1><Link href="/" className="mt-5 inline-block text-emerald-800 underline">Voltar à vitrine</Link></main>;

  return (
    <main className="bg-stone-50">
      <div className="mx-auto max-w-6xl px-5 py-10">
        <Link href="/" className="text-sm font-bold text-emerald-800">← Voltar aos imóveis</Link>
        <div className="mt-5 grid gap-8 lg:grid-cols-[1.4fr_0.8fr]">
          <div>
            <div className="relative h-72 overflow-hidden rounded-2xl bg-stone-200 sm:h-[440px]">
              <Image src={foto ? `/api${foto.url}` : "/imovel-placeholder.svg"} alt={imovel.titulo ?? "Imóvel"} fill priority className="object-cover" sizes="(max-width: 1024px) 100vw, 66vw" />
            </div>
            <section className="mt-7 rounded-2xl border border-stone-200 bg-white p-7">
              <p className="text-sm font-bold uppercase tracking-wider text-emerald-800">{imovel.tipoImovel?.nome ?? "Imóvel"} · #{imovel.id}</p>
              <h1 className="mt-2 text-3xl font-extrabold">{imovel.titulo}</h1>
              <p className="mt-2 text-stone-600">{imovel.endereco}, {imovel.numero} · {imovel.bairro?.nome}, {imovel.bairro?.cidade ?? "Cidade não informada"}</p>
              <p className="mt-6 leading-7 text-stone-700">{imovel.descricao}</p>
              <dl className="mt-7 grid grid-cols-2 gap-4 border-t border-stone-200 pt-6 sm:grid-cols-4">
                <div><dt className="text-sm text-stone-500">Dormitórios</dt><dd className="font-bold">{imovel.dormitorios ?? "—"}</dd></div>
                <div><dt className="text-sm text-stone-500">Banheiros</dt><dd className="font-bold">{imovel.banheiros ?? "—"}</dd></div>
                <div><dt className="text-sm text-stone-500">Garagem</dt><dd className="font-bold">{imovel.garagem ?? "—"}</dd></div>
                <div><dt className="text-sm text-stone-500">Área total</dt><dd className="font-bold">{imovel.areaTotal ? `${imovel.areaTotal} m²` : "—"}</dd></div>
              </dl>
              {imovel.caracteristicas && <p className="mt-6"><strong>Características:</strong> {imovel.caracteristicas}</p>}
            </section>
          </div>

          <aside>
            <section className="rounded-2xl bg-emerald-950 p-6 text-white">
              {imovel.precoVenda != null && <p><span className="text-sm text-emerald-200">Venda</span><strong className="block text-2xl">{moeda(imovel.precoVenda)}</strong></p>}
              {imovel.precoAluguel != null && <p className="mt-4"><span className="text-sm text-emerald-200">Aluguel mensal</span><strong className="block text-2xl">{moeda(imovel.precoAluguel)}</strong></p>}
            </section>
            <section className="mt-6 rounded-2xl border border-stone-200 bg-white p-6">
              <h2 className="text-xl font-bold">Entrar em contato</h2>
              <p className="mt-2 text-sm text-stone-600">A mensagem já contém os dados do imóvel.</p>
              <form onSubmit={enviarContato} noValidate className="mt-5 space-y-4">
                <label className="block text-sm font-semibold">Nome<input value={nome} onChange={(e) => setNome(e.target.value)} className="mt-1.5 w-full rounded-lg border border-stone-300 p-2.5 font-normal" /></label>
                <label className="block text-sm font-semibold">Email<input value={email} onChange={(e) => setEmail(e.target.value)} type="email" className="mt-1.5 w-full rounded-lg border border-stone-300 p-2.5 font-normal" /></label>
                <label className="block text-sm font-semibold">Telefone<input value={telefone} onChange={(e) => setTelefone(e.target.value)} className="mt-1.5 w-full rounded-lg border border-stone-300 p-2.5 font-normal" /></label>
                <label className="block text-sm font-semibold">Mensagem<textarea value={mensagem} onChange={(e) => setMensagem(e.target.value)} rows={5} className="mt-1.5 w-full rounded-lg border border-stone-300 p-2.5 font-normal" /></label>
                {feedback && <p role="status" className="rounded-lg bg-stone-100 p-3 text-sm">{feedback}</p>}
                <button disabled={enviando} className="w-full cursor-pointer rounded-lg bg-emerald-900 p-3 font-bold text-white disabled:opacity-60">{enviando ? "Enviando…" : "Enviar interesse"}</button>
              </form>
            </section>
          </aside>
        </div>
      </div>
    </main>
  );
}

export default function DetalheImovelPage() {
  return <Suspense fallback={<main className="mx-auto max-w-6xl px-5 py-16">Carregando imóvel…</main>}><DetalheImovel /></Suspense>;
}
