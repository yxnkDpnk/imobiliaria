export type Bairro = {
  id: number;
  nome: string;
  cidade: string | null;
  estado: string | null;
};

export type Foto = {
  id: number;
  nomeArquivo: string;
  capa: boolean | null;
  ordem: number | null;
  url: string;
};

export type Imovel = {
  id: number;
  titulo: string | null;
  descricao: string | null;
  status: string | null;
  finalidade: string | null;
  precoVenda: number | null;
  precoAluguel: number | null;
  dormitorios: number | null;
  banheiros: number | null;
  garagem: number | null;
  areaTotal: number | null;
  areaConstruida: number | null;
  endereco: string | null;
  numero: string | null;
  complemento: string | null;
  cep: string | null;
  caracteristicas: string | null;
  bairro: Bairro | null;
  tipoImovel: { id: number; nome: string; descricao: string | null } | null;
  fotos?: Foto[];
};

export type Perfil = "ADMIN" | "USER" | "CORRETOR";

export type Usuario = { id: number; nome: string; email: string; tipo: Perfil };

export type Oferta = {
  id: number;
  imovel: Imovel;
  usuario: Usuario;
  tipo: "VENDA" | "ALUGUEL";
  valorProposto: number;
  mensagem: string;
  status: "PENDENTE" | "ACEITA" | "EM_ESPERA" | "RECUSADA" | "CANCELADA";
  createdAt: string;
};

export type Contrato = {
  id: number;
  oferta: Oferta;
  status: "EM_APROVACAO" | "AGUARDANDO_ASSINATURA" | "ASSINADO" | "RECUSADO" | "CANCELADO";
  observacoes: string;
  nomeAssinatura: string | null;
  dataCriacao: string;
  dataAprovacao: string | null;
  dataEnvio: string | null;
  dataAssinatura: string | null;
  notificacaoStatus: string | null;
};

export type Painel = {
  imoveisDisponiveis: number;
  ofertasPendentes: number;
  contratosAguardandoAssinatura: number;
  imoveisPorBairro: { nome: string; total: number }[];
  ofertasPorSituacao: { nome: string; total: number }[];
  imoveisPorFinalidade: { nome: string; total: number }[];
};

export function areaDoPerfil(tipo: Perfil) {
  return tipo === "ADMIN" ? "/admin" : tipo === "CORRETOR" ? "/corretor" : "/cliente";
}

export type Sessao = {
  token: string;
  tipo: Perfil;
  nome: string;
  usuarioId: number;
};
