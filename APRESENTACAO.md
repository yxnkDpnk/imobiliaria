# Apresentação — checkpoint 1

1. **GitHub:** no terminal da raiz, execute `gh repo view --web`. Mostre o README e o primeiro commit.
2. **Segurança:** abra [apresentacao.http](primeiro-projeto/apresentacao.http). Clique em **Send Request** nos testes 01 a 05, em ordem. Resultados: **200, 200, 401, 403, 200**.
3. **Frontend:** abra [a vitrine](http://localhost:3000). Mostre a casa e seus preços vindos do banco.
4. **API:** abra [os dados do Spring](http://localhost:8080/imoveis). Explique: Next → Spring → MySQL.
5. **Código:** abra [a página inicial](imobiliaria-front/app/page.tsx) e procure `fetch`. Mostre a chamada e os estados de carregamento, vazio e erro.

Frase final: “Concluí o setup, os testes de segurança, a primeira integração e o versionamento. Usei IA como apoio. As próximas funcionalidades entram nos próximos checkpoints.”

## Atalhos no VS Code

- **Ctrl+P**, digite `apresentacao.http` e pressione Enter para abrir os testes.
- **Ctrl+Shift+V** neste arquivo abre o roteiro formatado, com links clicáveis.
- No HTTP, clique em **Send Request** acima de cada teste (extensão REST Client).

## Se precisar iniciar novamente

Terminal 1, na raiz: `cd primeiro-projeto && ./iniciar.sh`

Terminal 2, na raiz: `cd imobiliaria-front && npm run dev`

Os testes abaixo apenas autenticam e consultam; não modificam registros nem enviam email.
