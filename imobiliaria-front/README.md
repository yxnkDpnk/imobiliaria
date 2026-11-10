# Frontend da imobiliária

Frontend de Douglas Martins Erdmann feito com React, Next.js, TypeScript e Tailwind CSS.

## Executar

Com o backend Spring Boot na porta 8080:

```bash
npm ci
npm run dev
```

Abra http://localhost:3000.

## Funcionalidades da Aula 2

- Vitrine pública com filtros combinados, ordenação e paginação.
- Detalhes do imóvel e formulário de contato.
- Login JWT para cliente e administrador.
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
