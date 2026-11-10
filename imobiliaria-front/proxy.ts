import { NextRequest, NextResponse } from "next/server";

export function proxy(request: NextRequest) {
  const token = request.cookies.get("imobiliaria_token")?.value;
  const tipo = request.cookies.get("imobiliaria_tipo")?.value;
  const caminho = request.nextUrl.pathname;

  if (!token) return NextResponse.redirect(new URL("/login", request.url));
  if (caminho.startsWith("/admin") && tipo !== "ADMIN") {
    return NextResponse.redirect(new URL(tipo === "USER" ? "/cliente" : "/login", request.url));
  }
  if (caminho.startsWith("/cliente") && tipo !== "USER") {
    return NextResponse.redirect(new URL(tipo === "ADMIN" ? "/admin" : "/login", request.url));
  }
  return NextResponse.next();
}

export const config = { matcher: ["/cliente/:path*", "/admin/:path*"] };
