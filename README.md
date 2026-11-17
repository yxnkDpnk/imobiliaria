# Sistema de Imobiliária

Trabalho final individual de **Douglas Martins Erdmann**. O frontend em React e Next.js consome a API REST Spring Boot desenvolvida na disciplina.

## Estado atual — Aula 3

- Vitrine pública com fotos, tipo, bairro, cidade, finalidade e valores.
- Filtros combinados por bairro, finalidade e faixa de preço.
- Ordenação por preço e paginação da lista.
- Página de detalhes e formulário de contato com os dados do imóvel.
- Login com usuários do MySQL, senha BCrypt e JWT nos perfis cliente, corretor e administrador.
- Favoritos, propostas de compra e aluguel e acompanhamento das negociações.
- Contratos com preparação pelo corretor, aprovação e envio pelo administrador e assinatura pelo cliente.
- Contrato em PDF salvo no banco, com dados da negociação, nome e data da assinatura.
- Painéis com totais e gráficos de imóveis por bairro e finalidade e ofertas por situação.
- Gestão de imóveis, bairros, tipos, fotos e usuários, com confirmação antes de excluir.
- Áreas e menus protegidos por perfil.
- Logout, expiração de sessão e mensagens amigáveis para erros da API.
- Layout responsivo e estados de carregamento, lista vazia e falha.

O fluxo do checkpoint é: **cliente faz oferta → corretor aceita e prepara contrato → administrador aprova e envia → cliente assina**.

A assinatura é um aceite autenticado para demonstração acadêmica, registrado no banco. Não utiliza certificado digital.

## Tecnologias

- Frontend: Next.js 16.4.5, React 19.3.0, TypeScript e Tailwind CSS.
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

## Apresentação da Aula 3

O passo a passo está em [APRESENTACAO_AULA3.md](APRESENTACAO_AULA3.md). As requisições para acompanhar o mesmo fluxo estão em [primeiro-projeto/aula3.http](primeiro-projeto/aula3.http).

1. Cliente: abra um imóvel disponível, salve nos favoritos e envie uma oferta.
2. Corretor: aceite a oferta e gere o contrato com as observações.
3. Administrador: confira o PDF, aprove e envie para o cliente.
4. Cliente: abra Meus contratos, baixe o PDF, informe o nome e confirme a assinatura.
5. Administrador: confira os gráficos e o imóvel vendido ou alugado. O cancelamento do contrato libera o imóvel novamente.

Use Sair antes de trocar de perfil. Cada área tem um botão Atualizar para buscar mudanças feitas por outro perfil.

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

Os resultados de verificação desta entrega estão em [TESTES_AULA3.md](TESTES_AULA3.md).

## Testes da Aula 2

Abra [primeiro-projeto/aula2.http](primeiro-projeto/aula2.http) no VS Code com a extensão REST Client e execute as solicitações 01 a 09 na ordem. O arquivo testa:

1. consulta pública de imóveis;
2. login do administrador;
3. conteúdo e validade do JWT;
4. bloqueio sem token;
5. criação autorizada pelo administrador;
6. criação de cliente;
7. login do cliente criado;
8. bloqueio de operação administrativa para cliente;
9. rejeição de token adulterado.

As solicitações 10 e 11 removem os dois registros temporários.

## Verificar o código

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

## Problemas comuns

- **JWT_SECRET ausente:** inicie o backend com `./iniciar.sh`; o script cria e reutiliza uma chave local.
- **Falha no MySQL:** confira se o serviço, o banco e as credenciais de `application.properties` estão corretos.
- **Vitrine sem dados:** confirme que existe um imóvel com status disponível.
- **Frontend sem API:** mantenha o Spring na porta 8080 e o Next na porta 3000.
- **Sessão expirada:** faça login novamente; o token vale uma hora.

Arquivos com credenciais, uploads, dependências e builds estão excluídos pelo `.gitignore`.
