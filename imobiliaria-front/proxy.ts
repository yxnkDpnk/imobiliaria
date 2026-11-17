import { NextRequest, NextResponse } from "next/server";
import { areaDoPerfil, type Perfil } from "./app/types";

export function proxy(request: NextRequest) {
  const token = request.cookies.get("imobiliaria_token")?.value;
  const tipo = request.cookies.get("imobiliaria_tipo")?.value;
  const caminho = request.nextUrl.pathname;

  if (!token || !["ADMIN", "USER", "CORRETOR"].includes(tipo ?? "")) return NextResponse.redirect(new URL("/login", request.url));
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    if (!payload.exp || payload.exp * 1000 <= Date.now()) throw new Error("expirado");
  } catch {
    const resposta = NextResponse.redirect(new URL("/login?expirada=1", request.url));
    resposta.cookies.delete("imobiliaria_token");
    resposta.cookies.delete("imobiliaria_tipo");
    return resposta;
  }
  const area = areaDoPerfil(tipo as Perfil);
  if (!caminho.startsWith(area + "/") && caminho !== area) return NextResponse.redirect(new URL(area, request.url));
  return NextResponse.next();
}

export const config = { matcher: ["/cliente/:path*", "/admin/:path*", "/corretor/:path*"] };
