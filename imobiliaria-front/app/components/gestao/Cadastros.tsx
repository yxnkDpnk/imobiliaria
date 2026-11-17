"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { api } from "@/app/lib/api";
import { useAuth } from "@/app/context/AuthContext";
import type { Foto, Imovel } from "@/app/types";

const botao = "cursor-pointer rounded-lg bg-emerald-900 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-800 disabled:opacity-50";
const secundario = "cursor-pointer rounded-lg border border-stone-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-stone-100 disabled:opacity-50";
const campo = "mt-1.5 w-full rounded-lg border border-stone-300 bg-white p-2.5 font-normal";
type Registro = { id: number; [chave: string]: unknown };
type Recurso = "imoveis" | "bairros" | "tipos-imovel" | "users";
type Campo = { chave: string; nome: string; tipo?: string; obrigatorio?: boolean; opcoes?: [string, string][] };
const titulos: Record<Recurso, string> = { imoveis: "Imóveis", bairros: "Bairros", "tipos-imovel": "Tipos de imóvel", users: "Usuários" };
const campos: Record<Recurso, Campo[]> = {
  imoveis: [
    { chave: "titulo", nome: "Título", obrigatorio: true }, { chave: "descricao", nome: "Descrição", tipo: "textarea", obrigatorio: true },
    { chave: "finalidade", nome: "Finalidade", tipo: "select", obrigatorio: true, opcoes: [["VENDA", "Venda"], ["ALUGUEL", "Aluguel"], ["VENDA_ALUGUEL", "Venda e aluguel"]] },
    { chave: "precoVenda", nome: "Preço de venda (R$)", tipo: "number" }, { chave: "precoAluguel", nome: "Aluguel mensal (R$)", tipo: "number" },
    { chave: "bairroId", nome: "Bairro", tipo: "select", obrigatorio: true }, { chave: "tipoImovelId", nome: "Tipo de imóvel", tipo: "select", obrigatorio: true },
    { chave: "endereco", nome: "Endereço", obrigatorio: true }, { chave: "numero", nome: "Número", obrigatorio: true }, { chave: "complemento", nome: "Complemento" }, { chave: "cep", nome: "CEP" },
    { chave: "dormitorios", nome: "Dormitórios", tipo: "number" }, { chave: "banheiros", nome: "Banheiros", tipo: "number" }, { chave: "garagem", nome: "Vagas de garagem", tipo: "number" },
    { chave: "areaTotal", nome: "Área total (m²)", tipo: "number" }, { chave: "areaConstruida", nome: "Área construída (m²)", tipo: "number" },
    { chave: "caracteristicas", nome: "Características", tipo: "textarea" },
    { chave: "destaque", nome: "Destaque", tipo: "select", opcoes: [["false", "Não"], ["true", "Sim"]] },
  ],
    bairros: [{ chave: "nome", nome: "Nome", obrigatorio: true }, { chave: "cidade", nome: "Cidade", obrigatorio: true }, { chave: "estado", nome: "Estado (UF)", obrigatorio: true }, { chave: "cepInicial", nome: "CEP inicial" }, { chave: "cepFinal", nome: "CEP final" }],
  "tipos-imovel": [{ chave: "nome", nome: "Nome", obrigatorio: true }, { chave: "descricao", nome: "Descrição", tipo: "textarea" }],
  users: [{ chave: "nome", nome: "Nome", obrigatorio: true }, { chave: "email", nome: "Email", tipo: "email", obrigatorio: true }, { chave: "senha", nome: "Senha (deixe vazia para manter ao editar)", tipo: "password" }, { chave: "tipo", nome: "Perfil", tipo: "select", obrigatorio: true, opcoes: [["USER", "Cliente"], ["CORRETOR", "Corretor"], ["ADMIN", "Administrador"]] }],
};
function texto(valor: unknown) { return valor == null ? "" : String(valor); }

function CadastroRecurso({ recurso }: { recurso: Recurso }) {
  const { sessao } = useAuth();
  const [registros, setRegistros] = useState<Registro[]>([]);
  const [bairros, setBairros] = useState<Registro[]>([]);
  const [tipos, setTipos] = useState<Registro[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [aviso, setAviso] = useState("");
  const [formulario, setFormulario] = useState<Record<string, string>>({});
  const [editando, setEditando] = useState<Registro | null>(null);
  const [aberto, setAberto] = useState(false);
  const [ocupado, setOcupado] = useState(false);
  const [busca, setBusca] = useState("");
  const [pagina, setPagina] = useState(1);
  const carregar = useCallback(async () => {
    setCarregando(true); setErro("");
    try {
      const lista = await api<Registro[]>(`/${recurso}`); setRegistros(lista);
      if (recurso === "imoveis") { const [listaBairros, listaTipos] = await Promise.all([api<Registro[]>("/bairros"), api<Registro[]>("/tipos-imovel")]); setBairros(listaBairros); setTipos(listaTipos); }
    } catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível carregar os cadastros."); }
    finally { setCarregando(false); }
  }, [recurso]);
  useEffect(() => { const timer = window.setTimeout(() => void carregar(), 0); return () => window.clearTimeout(timer); }, [carregar]);

  function abrir(registro: Registro | null) {
    setAviso(""); setEditando(registro);
    const valores: Record<string, string> = {};
    for (const item of campos[recurso]) valores[item.chave] = registro ? texto(registro[item.chave]) : "";
    if (registro && recurso === "imoveis") {
      valores.bairroId = texto((registro.bairro as { id?: number } | null)?.id);
      valores.tipoImovelId = texto((registro.tipoImovel as { id?: number } | null)?.id);
      const status = texto(registro.status).normalize("NFD").replace(/[\u0300-\u036f]/g, "").toUpperCase();
      valores.status = status;
      if (!campos.imoveis[2].opcoes?.some(([valor]) => valor === valores.finalidade)) valores.finalidade = registro.precoVenda && registro.precoAluguel ? "VENDA_ALUGUEL" : registro.precoAluguel ? "ALUGUEL" : "VENDA";
    }
    if (!registro) { valores.status = "DISPONIVEL"; valores.finalidade = "VENDA"; valores.tipo = "USER"; valores.destaque = "false"; }
    valores.senha = ""; setFormulario(valores); setAberto(true);
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault(); setAviso("");
    if (campos[recurso].some((item) => item.obrigatorio && !formulario[item.chave]?.trim())) { setAviso("Preencha todos os campos obrigatórios."); return; }
    if (recurso === "users" && (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formulario.email) || (!editando && !formulario.senha?.trim()))) { setAviso("Informe email válido e senha para o novo usuário."); return; }
    if (recurso === "bairros" && !/^[A-Za-z]{2}$/.test(formulario.estado)) { setAviso("Use duas letras na sigla do estado."); return; }
    if (recurso === "imoveis" && !Number(formulario.precoVenda) && !Number(formulario.precoAluguel)) { setAviso("Informe ao menos um preço maior que zero."); return; }
    if (recurso === "imoveis" && ((["VENDA", "VENDA_ALUGUEL"].includes(formulario.finalidade) && !(Number(formulario.precoVenda) > 0)) || (["ALUGUEL", "VENDA_ALUGUEL"].includes(formulario.finalidade) && !(Number(formulario.precoAluguel) > 0)))) { setAviso("Informe um preço positivo para cada finalidade selecionada."); return; }
    if (campos[recurso].some((item) => item.tipo === "number" && formulario[item.chave] && (!Number.isFinite(Number(formulario[item.chave])) || Number(formulario[item.chave]) < 0))) { setAviso("Os valores numéricos devem ser válidos e não negativos."); return; }
    if (recurso === "imoveis" && ["dormitorios", "banheiros", "garagem"].some((chave) => formulario[chave] && !Number.isInteger(Number(formulario[chave])))) { setAviso("Dormitórios, banheiros e vagas devem ser números inteiros."); return; }
    const corpo: Record<string, unknown> = {};
    for (const item of campos[recurso]) { const valor = formulario[item.chave]; corpo[item.chave] = item.tipo === "number" ? (valor ? Number(valor) : editando ? 0 : null) : (valor?.trim() ?? ""); }
    if (recurso === "imoveis") { corpo.bairro = { id: Number(formulario.bairroId) }; corpo.tipoImovel = { id: Number(formulario.tipoImovelId) }; corpo.destaque = formulario.destaque === "true"; delete corpo.bairroId; delete corpo.tipoImovelId; if (editando) corpo.usuario = editando.usuario; }
    if (recurso === "bairros") corpo.estado = formulario.estado.toUpperCase();
    if (editando) corpo.id = editando.id;
    const caminho = `/${recurso}${editando && ["imoveis", "users"].includes(recurso) ? `/${editando.id}` : ""}`;
    setOcupado(true);
    try { await api(caminho, { method: editando ? "PUT" : "POST", body: JSON.stringify(corpo) }); setAberto(false); setAviso("Cadastro salvo com sucesso."); await carregar(); }
    catch (e) { setAviso(e instanceof Error ? e.message : "Não foi possível salvar o cadastro."); }
    finally { setOcupado(false); }
  }

  async function excluir(registro: Registro) {
    if (!confirm(`Excluir ${texto(registro.titulo ?? registro.nome)}? Esta ação não pode ser desfeita.`)) return;
    setOcupado(true); setAviso("");
    try { await api(`/${recurso}/${registro.id}`, { method: "DELETE" }); setAviso("Cadastro excluído."); await carregar(); }
    catch (e) { setAviso(e instanceof Error ? e.message : "Não foi possível excluir o cadastro."); }
    finally { setOcupado(false); }
  }

  const encontrados = registros.filter((registro) => `${texto(registro.nome)} ${texto(registro.email)} ${texto(registro.titulo)} ${texto(registro.cidade)}`.toLowerCase().includes(busca.toLowerCase()));
  const totalPaginas = Math.max(1, Math.ceil(encontrados.length / 6));
  const paginaAtual = Math.min(pagina, totalPaginas);
  return <section>
    <div className="mb-5 flex flex-wrap items-center justify-between gap-3"><h2 className="text-xl font-bold">{titulos[recurso]}</h2><button className={botao} disabled={ocupado} onClick={() => abrir(null)}>Novo cadastro</button></div>
    {aviso && <p role="status" className="mb-4 rounded-lg bg-emerald-50 p-4 text-emerald-950">{aviso}</p>}
    {aberto && <form onSubmit={salvar} noValidate className="mb-7 rounded-2xl border border-stone-200 bg-stone-50 p-6"><h3 className="mb-5 text-lg font-bold">{editando ? `Editar #${editando.id}` : "Novo cadastro"}</h3><div className="grid gap-4 sm:grid-cols-2">{campos[recurso].map((item) => {
      const opcoes = item.chave === "bairroId" ? bairros.map((registro) => [String(registro.id), `${texto(registro.nome)} · ${texto(registro.cidade)}`]) : item.chave === "tipoImovelId" ? tipos.map((registro) => [String(registro.id), texto(registro.nome)]) : item.opcoes;
      return <label key={item.chave} className={`block text-sm font-semibold ${item.tipo === "textarea" ? "sm:col-span-2" : ""}`}>{item.nome}{item.obrigatorio ? " *" : ""}{item.tipo === "select" ? <select className={campo} value={formulario[item.chave] ?? ""} onChange={(e) => setFormulario({ ...formulario, [item.chave]: e.target.value })}><option value="">Selecione</option>{opcoes?.map(([valor, nome]) => <option key={valor} value={valor}>{nome}</option>)}</select> : item.tipo === "textarea" ? <textarea rows={3} className={campo} maxLength={5000} value={formulario[item.chave] ?? ""} onChange={(e) => setFormulario({ ...formulario, [item.chave]: e.target.value })} /> : <input type={item.tipo ?? "text"} min={item.tipo === "number" ? 0 : undefined} step={item.tipo === "number" ? (["dormitorios", "banheiros", "garagem"].includes(item.chave) ? "1" : "0.01") : undefined} autoComplete={item.tipo === "password" ? "new-password" : undefined} maxLength={item.chave === "estado" ? 2 : undefined} className={campo} value={formulario[item.chave] ?? ""} onChange={(e) => setFormulario({ ...formulario, [item.chave]: e.target.value })} />}</label>;
    })}</div><div className="mt-5 flex gap-3"><button disabled={ocupado} className={botao}>{ocupado ? "Salvando…" : "Salvar"}</button><button type="button" disabled={ocupado} className={secundario} onClick={() => setAberto(false)}>Cancelar edição</button></div></form>}
    <label className="mb-5 block max-w-md text-sm font-semibold">Buscar por {recurso === "users" ? "nome ou email" : "nome"}<input type="search" className={campo} value={busca} onChange={(e) => { setBusca(e.target.value); setPagina(1); }} placeholder="Digite para filtrar" /></label>
    {carregando && <p role="status" className="rounded-lg bg-stone-100 p-5">Carregando cadastros…</p>}
    {!carregando && erro && <div role="alert" className="rounded-lg bg-red-50 p-4"><p>{erro}</p><button className={`${secundario} mt-3`} onClick={() => void carregar()}>Tentar novamente</button></div>}
    {!carregando && !erro && encontrados.length === 0 && <p className="rounded-lg bg-stone-100 p-5">Nenhum cadastro encontrado.</p>}
    {!carregando && !erro && <><div className="space-y-3">{encontrados.slice((paginaAtual - 1) * 6, paginaAtual * 6).map((registro) => <article key={registro.id} className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-stone-200 p-5"><div><p className="text-xs text-stone-500">#{registro.id}</p><h3 className="font-bold">{texto(registro.titulo ?? registro.nome)}</h3><p className="mt-1 text-sm text-stone-600">{recurso === "users" ? `${texto(registro.email)} · ${texto(registro.tipo)}` : recurso === "imoveis" ? `${texto(registro.status)} · ${(registro.bairro as { nome?: string } | null)?.nome ?? ""}` : texto(registro.cidade ?? registro.descricao)}</p></div><div className="flex gap-2"><button className={secundario} disabled={ocupado} onClick={() => abrir(registro)}>Editar</button><button className={secundario} disabled={ocupado || (recurso === "users" && registro.id === sessao?.usuarioId)} title={recurso === "users" && registro.id === sessao?.usuarioId ? "Você não pode excluir sua própria conta" : undefined} onClick={() => void excluir(registro)}>Excluir</button></div></article>)}</div>{encontrados.length > 6 && <nav aria-label="Páginas de cadastros" className="mt-5 flex items-center justify-center gap-4"><button className={secundario} disabled={paginaAtual === 1} onClick={() => setPagina(paginaAtual - 1)}>Anterior</button><span>{paginaAtual} / {totalPaginas}</span><button className={secundario} disabled={paginaAtual === totalPaginas} onClick={() => setPagina(paginaAtual + 1)}>Próxima</button></nav>}</>}
  </section>;
}

function CadastroFotos() {
  const [imoveis, setImoveis] = useState<Imovel[]>([]);
  const [imovelId, setImovelId] = useState("");
  const [fotos, setFotos] = useState<Foto[]>([]);
  const [ordens, setOrdens] = useState<Record<number, string>>({});
  const [arquivo, setArquivo] = useState<File | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [aviso, setAviso] = useState("");
  const [erro, setErro] = useState("");
  const [ocupado, setOcupado] = useState(false);
  const [tentativa, setTentativa] = useState(0);
  useEffect(() => { let ativo = true; api<Imovel[]>("/imoveis").then((lista) => { if (ativo) setImoveis(lista); }).catch((e) => { if (ativo) setErro(e instanceof Error ? e.message : "Falha ao buscar imóveis."); }).finally(() => { if (ativo) setCarregando(false); }); return () => { ativo = false; }; }, [tentativa]);
  const carregarFotos = useCallback(async () => {
    if (!imovelId) { setFotos([]); return; }
    setCarregando(true); setErro("");
    try { const lista = await api<Foto[]>(`/fotos/imovel/${imovelId}`); setFotos(lista); setOrdens(Object.fromEntries(lista.map((foto) => [foto.id, String(foto.ordem ?? 0)]))); }
    catch (e) { setErro(e instanceof Error ? e.message : "Não foi possível carregar as fotos."); }
    finally { setCarregando(false); }
  }, [imovelId]);
  useEffect(() => { const timer = window.setTimeout(() => void carregarFotos(), 0); return () => window.clearTimeout(timer); }, [carregarFotos]);
  async function executar(caminho: string, method: string, body?: BodyInit) {
    setOcupado(true); setAviso("");
    try { await api(caminho, { method, body }); setAviso("Foto atualizada com sucesso."); await carregarFotos(); }
    catch (e) { setAviso(e instanceof Error ? e.message : "Não foi possível atualizar a foto."); }
    finally { setOcupado(false); }
  }
  function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!arquivo || !imovelId) { setAviso("Selecione o imóvel e um arquivo de imagem."); return; }
    if (!["image/png", "image/jpeg", "image/gif"].includes(arquivo.type) || arquivo.size > 10 * 1024 * 1024) { setAviso("Escolha uma imagem PNG, JPEG ou GIF com até 10 MB."); return; }
    const dados = new FormData(); dados.append("imovelId", imovelId); dados.append("arquivo", arquivo); void executar("/fotos", "POST", dados);
  }
  return <section><h2 className="mb-5 text-xl font-bold">Fotos dos imóveis</h2>{aviso && <p role="status" className="mb-4 rounded-lg bg-emerald-50 p-4">{aviso}</p>}<label className="block max-w-lg text-sm font-semibold">Imóvel<select className={campo} value={imovelId} onChange={(e) => setImovelId(e.target.value)}><option value="">Selecione um imóvel</option>{imoveis.map((imovel) => <option key={imovel.id} value={imovel.id}>#{imovel.id} · {imovel.titulo}</option>)}</select></label>{imovelId && <form onSubmit={enviar} className="my-6 flex flex-wrap items-end gap-3 rounded-xl bg-stone-100 p-5"><label className="block text-sm font-semibold">Nova foto PNG, JPEG ou GIF (até 10 MB)<input type="file" accept="image/png,image/jpeg,image/gif" className={campo} onChange={(e) => setArquivo(e.target.files?.[0] ?? null)} /></label><button disabled={ocupado} className={botao}>{ocupado ? "Enviando…" : "Adicionar foto"}</button></form>}{carregando && <p role="status" className="mt-5">Carregando fotos…</p>}{erro && <div role="alert" className="my-4 rounded-xl bg-red-50 p-4"><p>{erro}</p><button className={`${secundario} mt-3`} onClick={() => { setTentativa((valor) => valor + 1); void carregarFotos(); }}>Tentar novamente</button></div>}{!carregando && !erro && imovelId && fotos.length === 0 && <p className="my-4 rounded-xl bg-stone-100 p-5">Nenhuma foto cadastrada para este imóvel.</p>}<div className="mt-5 space-y-3">{!carregando && !erro && fotos.map((foto) => <article key={foto.id} className="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-stone-200 p-5"><div className="min-w-0"><a href={`/api${foto.url}`} target="_blank" rel="noreferrer" className="break-all font-semibold text-emerald-800 underline">Abrir foto #{foto.id}</a><p className="mt-1 text-sm">{foto.capa ? "Foto de capa" : "Foto da galeria"}</p></div><div className="flex flex-wrap items-center gap-2"><label className="text-sm">Ordem<input aria-label={`Ordem da foto ${foto.id}`} type="number" min="0" step="1" value={ordens[foto.id] ?? "0"} className="ml-2 w-20 rounded-lg border border-stone-300 p-2" onChange={(e) => setOrdens({ ...ordens, [foto.id]: e.target.value })} /></label><button disabled={ocupado} className={secundario} onClick={() => { const valor = Number(ordens[foto.id]); if (!Number.isInteger(valor) || valor < 0) setAviso("A ordem deve ser um inteiro não negativo."); else void executar(`/fotos/${foto.id}`, "PUT", JSON.stringify({ ordem: valor })); }}>Salvar ordem</button><button disabled={ocupado || !!foto.capa} className={secundario} onClick={() => void executar(`/fotos/${foto.id}/capa`, "PUT")}>Definir capa</button><button disabled={ocupado} className={secundario} onClick={() => { if (confirm("Excluir esta foto?")) void executar(`/fotos/${foto.id}`, "DELETE"); }}>Excluir</button></div></article>)}</div></section>;
}

export default function Cadastros() {
  const [recurso, setRecurso] = useState<Recurso | "fotos">("imoveis");
  return <><nav aria-label="Tipos de cadastro" className="mb-7 flex flex-wrap gap-2">{([...Object.entries(titulos), ["fotos", "Fotos"]] as [Recurso | "fotos", string][]).map(([chave, nome]) => <button key={chave} className={recurso === chave ? botao : secundario} onClick={() => setRecurso(chave)}>{nome}</button>)}</nav>{recurso === "fotos" ? <CadastroFotos /> : <CadastroRecurso key={recurso} recurso={recurso} />}</>;
}
