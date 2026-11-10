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

export type Perfil = "ADMIN" | "USER";

export type Sessao = {
  token: string;
  tipo: Perfil;
  nome: string;
  usuarioId: number;
};
