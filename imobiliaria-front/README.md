# Frontend da imobiliária

Frontend de Douglas Martins Erdmann feito com React, Next.js, TypeScript e Tailwind CSS.

## Executar

Com o backend Spring Boot na porta 8080:

```bash
npm ci
npm run dev
```

Abra http://localhost:3000.

## Funcionalidades

- Vitrine pública com filtros combinados, ordenação e paginação.
- Detalhes do imóvel e formulário de contato.
- Login JWT para cliente, corretor e administrador.
- Favoritos e ofertas de compra ou aluguel.
- Contratos: preparar, aprovar, enviar, baixar PDF, assinar e cancelar.
- Painéis com gráficos e totais reais da API.
- CRUD de imóveis, bairros, tipos, usuários e fotos.
- Rotas protegidas e menu de acordo com o perfil.
- Logout e tratamento de sessão expirada.
- Estados de carregamento, vazio e erro amigável.
- Interface responsiva.

O rewrite de `next.config.ts` encaminha `/api/*` para `http://127.0.0.1:8080/*`.

## Verificar

```bash
npx tsc --noEmit
npm run lint
npm run build
```

As instruções do banco, backend, credenciais de demonstração e testes HTTP estão no [README principal](../README.md).

O roteiro do checkpoint está em [APRESENTACAO_AULA3.md](../APRESENTACAO_AULA3.md).
