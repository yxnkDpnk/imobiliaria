# Sistema de Imobiliária

Trabalho final individual de **Douglas Martins Erdmann**. O frontend em React e Next.js consome a API REST Spring Boot desenvolvida na disciplina.

## Funcionalidades

- Consulta de imóveis com fotos, detalhes, filtros, ordenação e paginação.
- Login JWT nos perfis cliente, corretor e administrador.
- Favoritos e propostas de compra ou aluguel.
- Preparação, aprovação, envio e assinatura de contratos, com PDF salvo no banco.
- Painel com totais e gráficos.
- Cadastro e edição de imóveis, bairros, tipos, fotos e usuários.

A assinatura registra o aceite, o nome do cliente e a data. Não utiliza certificado digital.

## Tecnologias

- Frontend: Next.js 16.4.6, React 19.3.0, TypeScript e Tailwind CSS.
- Backend: Java, Spring Boot 4.1.1, Spring Data JPA, Spring Security e JJWT.
- Banco: MySQL.

Requisitos para executar: Java 17 ou superior, Node.js 24 com npm, MySQL 8, Bash e OpenSSL. O projeto inclui o Maven Wrapper.

## Estrutura

```text
primeiro-projeto/       API Spring Boot e arquivos de teste HTTP
imobiliaria-front/      Frontend Next.js
banco/                  Dump do banco MySQL
```

## Preparar o banco

Crie e importe o banco fornecido:

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS projeto_spring CHARACTER SET utf8mb4;"
mysql -u root -p projeto_spring < banco/projeto_spring.sql
```

O dump inclui os imóveis de exemplo para filtros e paginação, os três perfis de acesso e as tabelas `ofertas`, `contratos` e `favoritos`.

Se a configuração local ainda não existir:

```bash
cp -n primeiro-projeto/src/main/resources/application.properties.example primeiro-projeto/src/main/resources/application.properties
```

Edite `application.properties` com o usuário e a senha do seu MySQL. Para enviar emails, configure o SMTP e `app.imobiliaria-email`. Senhas e chaves locais não são versionadas.

Para ativar a notificação de contratos por email, configure `app.notificacoes-email=true` e as credenciais SMTP. Com a opção desativada, o contrato continua disponível na área do cliente e o painel informa a notificação pendente. Em caso de falha do SMTP, o painel informa a falha sem perder o contrato. O modo de teste não envia mensagens externas.

Para testar o envio real, use um cliente com endereço de email válido e permitido pelo seu provedor SMTP.

## Executar

Terminal 1 — backend:

```bash
cd primeiro-projeto
./iniciar.sh
```

Terminal 2 — frontend:

```bash
cd imobiliaria-front
npm ci
npm run dev
```

Abra http://localhost:3000. O frontend encaminha `/api/*` para o Spring em `http://127.0.0.1:8080`.

## Acessos de demonstração

| Perfil | Email | Senha |
|---|---|---|
| Administrador | `admin@imobiliaria.com` | `1234` |
| Cliente | `cliente@imobiliaria.com` | `1234` |
| Corretor | `corretor@imobiliaria.com` | `1234` |

Essas credenciais são somente para demonstração acadêmica.

## Regras da negociação

- Apenas imóveis disponíveis recebem ofertas. O tipo precisa ter preço anunciado no imóvel.
- O mesmo cliente não pode ter duas ofertas pendentes para o mesmo imóvel.
- Clientes diferentes podem enviar ofertas ao mesmo imóvel.
- Ao aceitar uma proposta, o imóvel fica reservado e as outras pendentes entram em espera.
- O contrato só pode ser assinado pelo cliente da oferta, após aprovação e envio pelo administrador.
- Ao assinar, o imóvel fica vendido ou alugado e as outras propostas em espera são recusadas.
- Ao cancelar o contrato, a oferta aceita é cancelada, o imóvel volta a disponível e propostas em espera voltam a pendentes.
- Cada cliente só consulta suas próprias ofertas, favoritos e contratos enviados.
- O administrador não pode excluir a própria conta nem retirar seu próprio perfil administrativo.
- Requisições que violam regras recebem uma mensagem de negócio com HTTP 409. Perfis sem permissão recebem 403.

As decisões sobre o mesmo imóvel usam transação e bloqueio no banco para evitar duas reservas ao mesmo tempo.

## Testes

As requisições podem ser executadas no VS Code com a extensão REST Client:

- [aula2.http](primeiro-projeto/aula2.http): login e permissões.
- [aula3.http](primeiro-projeto/aula3.http): ofertas, contratos e regras da negociação.

Backend:

```bash
cd primeiro-projeto
./mvnw test
```

Frontend:

```bash
cd imobiliaria-front
npx tsc --noEmit
npm run lint
npm run build
```

Arquivos com credenciais, uploads, dependências e builds estão excluídos pelo `.gitignore`.
