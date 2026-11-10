# Sistema de Imobiliária

Trabalho final individual de **Douglas Martins Erdmann**. O frontend em React e Next.js consome a API REST Spring Boot desenvolvida na disciplina.

## Estado atual — Aula 2

- Vitrine pública com fotos, tipo, bairro, cidade, finalidade e valores.
- Filtros combinados por bairro, finalidade e faixa de preço.
- Ordenação por preço e paginação da lista.
- Página de detalhes e formulário de contato com os dados do imóvel.
- Login real com usuários do MySQL, senha BCrypt e JWT com perfil `ADMIN` ou `USER`.
- Áreas iniciais de cliente e administrador com rotas e menus protegidos por perfil.
- Logout, expiração de sessão e mensagens amigáveis para erros da API.
- Layout responsivo e estados de carregamento, lista vazia e falha.

Ofertas, favoritos, contratos, CRUDs administrativos e gráficos pertencem às próximas aulas.

## Tecnologias

- Frontend: Next.js 16.4.5, React 19.3.0, TypeScript e Tailwind CSS.
- Backend: Java, Spring Boot 4.1.1, Spring Data JPA, Spring Security e JJWT.
- Banco: MySQL.

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

Se a configuração local ainda não existir:

```bash
cp -n primeiro-projeto/src/main/resources/application.properties.example primeiro-projeto/src/main/resources/application.properties
```

Edite `application.properties` com o usuário e a senha do seu MySQL. Para enviar emails, configure o SMTP e `app.imobiliaria-email`. Senhas e chaves locais não são versionadas.

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

Essas credenciais são somente para demonstração acadêmica.

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
