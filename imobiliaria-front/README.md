# Frontend da imobiliária

Frontend de Douglas Martins Erdmann com React, Next.js, TypeScript e Tailwind CSS.

As instruções completas do backend, banco, configuração e testes estão no [README principal](../README.md).

## Executar

Com o Spring Boot rodando na porta 8080:

```bash
npm ci
npm run dev
```

Abra http://localhost:3000. A página inicial em `app/page.tsx` lista os imóveis disponíveis. O rewrite em `next.config.ts` encaminha `/api/imoveis` para o Spring.

## Verificar

```bash
npx tsc --noEmit
npm run build
```

A versão inicial inclui carregamento, lista vazia, erro e nova tentativa. Fotos, detalhes, login e demais telas serão implementados nas próximas etapas.
