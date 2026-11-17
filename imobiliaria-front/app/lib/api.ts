export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

export function limparSessao() {
  if (typeof window === "undefined") return;
  localStorage.removeItem("imobiliaria_sessao");
  document.cookie = "imobiliaria_token=; path=/; max-age=0; samesite=lax";
  document.cookie = "imobiliaria_tipo=; path=/; max-age=0; samesite=lax";
}

function mensagemPadrao(status: number) {
  if (status === 401) return "Sua sessão expirou. Entre novamente.";
  if (status === 403) return "Você não tem permissão para esta ação.";
  if (status === 404) return "O item solicitado não foi encontrado.";
  if (status === 409) return "Esta operação não pode ser concluída.";
  return "O servidor não conseguiu concluir a solicitação.";
}

async function requisicao(caminho: string, init: RequestInit = {}): Promise<Response> {
  const sessao = typeof window === "undefined"
    ? null
    : localStorage.getItem("imobiliaria_sessao");
  let token: string | null = null;
  if (sessao) {
    try {
      token = JSON.parse(sessao).token as string;
    } catch {
      limparSessao();
    }
  }
  const headers = new Headers(init.headers);
  if (token) headers.set("Authorization", `Bearer ${token}`);
  if (init.body && !(init.body instanceof FormData)) headers.set("Content-Type", "application/json");

  let resposta: Response;
  try {
    resposta = await fetch(`/api${caminho}`, { ...init, headers, cache: "no-store" });
  } catch {
    throw new ApiError(0, "Não foi possível conectar ao servidor.");
  }

  if (!resposta.ok) {
    const texto = await resposta.text();
    let mensagem = mensagemPadrao(resposta.status);
    if (texto) {
      try {
        const json = JSON.parse(texto) as { erro?: string; message?: string; detail?: string };
        mensagem = json.erro ?? json.message ?? json.detail ?? mensagem;
      } catch {
        if (!texto.trim().startsWith("<")) mensagem = texto.slice(0, 300);
      }
    }
    if (resposta.status === 401 && token) {
      limparSessao();
      sessionStorage.setItem("imobiliaria_expirada", "1");
      window.dispatchEvent(new Event("imobiliaria:sessao-expirada"));
    }
    throw new ApiError(resposta.status, mensagem);
  }

  return resposta;
}

export async function baixarArquivo(caminho: string, nome: string): Promise<void> {
  const resposta = await requisicao(caminho);
  const url = URL.createObjectURL(await resposta.blob());
  const link = document.createElement("a");
  link.href = url;
  link.download = nome;
  link.click();
  window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export async function api<T>(caminho: string, init: RequestInit = {}): Promise<T> {
  const resposta = await requisicao(caminho, init);

  if (resposta.status === 204) return undefined as T;
  const texto = await resposta.text();
  if (!texto) return undefined as T;
  try {
    return JSON.parse(texto) as T;
  } catch {
    return texto as T;
  }
}
