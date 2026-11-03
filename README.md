# Sistema de Imobiliária

Trabalho final individual de **Douglas Martins Erdmann**: desenvolvimento de um frontend com React e Next.js que consome a API REST Spring Boot construída na disciplina.

## Estado atual — primeiro checkpoint

- Backend com autenticação JWT e perfis ADMIN e USER.
- API de imóveis, bairros, tipos de imóveis, usuários e fotos; serviço de email.
- Primeira integração do frontend com a API: listagem de imóveis disponíveis, tipo, bairro, descrição e preços em reais.
- Estados de carregamento, nenhum resultado, erro e botão para tentar novamente.
- Verificações manuais de consultas, autenticação, autorização e criação/alteração/exclusão de um imóvel temporário.

Ainda serão desenvolvidos login no frontend, detalhes, filtros, paginação de imóveis, favoritos, ofertas, contratos e área administrativa com gráficos. As fotos ainda não são exibidas na vitrine. A listagem já funciona, mas não representa a entrega final completa.

## Tecnologias

- Frontend: Next.js 16.4.5, React 19.3.0, TypeScript e Tailwind CSS.
- Backend: Java 17, Spring Boot 4.1.1, Spring Data JPA, Spring Security, JJWT, Thymeleaf e Spring Mail.
- Banco: MySQL.
- Ambiente utilizado: Linux, Node.js 24 e npm 11; Maven Wrapper incluído no backend.

## Estrutura

```text
primeiro-projeto/       API Spring Boot e testes HTTP
imobiliaria-front/      Frontend Next.js (App Router na pasta app/)
README.md              Instruções do projeto
```

## Pré-requisitos

- Java 17, MySQL em execução e Node.js 24 com npm.
- Bash, OpenSSL e ferramentas exigidas pelo Maven Wrapper (como curl) no Linux.
- Conexão com a internet na primeira instalação das dependências.
- Opcional: extensão REST Client do VS Code para executar `teste.http`.

## Preparar o banco e a configuração

Crie um banco MySQL chamado `projeto_spring`:

```sql
CREATE DATABASE IF NOT EXISTS projeto_spring
  CHARACTER SET utf8mb4;
```

Na raiz do repositório, copie o exemplo de configuração **somente se o arquivo local ainda não existir**:

```bash
cp -n primeiro-projeto/src/main/resources/application.properties.example primeiro-projeto/src/main/resources/application.properties
```

Edite `application.properties` com seu usuário e senha MySQL. Para envio de emails, preencha também as credenciais SMTP do Resend. Mantenha `jwt.secret=${JWT_SECRET}`: o script de inicialização fornece essa variável.

O Hibernate está configurado com `ddl-auto=update` e atualiza as tabelas ao iniciar. Os registros do banco local não são incluídos neste repositório. Com banco vazio, a vitrine mostra uma mensagem de nenhum imóvel disponível. Para testar com dados, cadastre um imóvel pela API com ADMIN; o passo 21 de `teste.http` é um exemplo. Esse registro será removido caso você execute o passo 25.

## Executar o backend

Em um terminal, a partir da raiz:

```bash
cd primeiro-projeto
./iniciar.sh
```

O script encerra uma instância anterior da aplicação Java executada neste diretório, gera uma chave JWT na primeira execução, reutiliza a chave local e executa:

```bash
./mvnw clean compile spring-boot:run
```

A chave fica em `.local/jwt-secret`, fora do versionamento. Para parar o backend, pressione `Ctrl+C`.

API: http://localhost:8080/imoveis

## Executar o frontend

Em **outro terminal**, a partir da raiz:

```bash
cd imobiliaria-front
npm ci
npm run dev
```

O `npm ci` instala as dependências do arquivo de lock; repita ao clonar o projeto ou quando suas dependências mudarem.

Abra http://localhost:3000 e mantenha os dois servidores em execução.

A página chama `/api/imoveis` no Next, que encaminha a requisição para `http://127.0.0.1:8080/imoveis` por um rewrite em `imobiliaria-front/next.config.ts`. Se a porta ou o endereço do Spring mudar, atualize esse destino.

## Testar a API

Abra `primeiro-projeto/teste.http` no VS Code com REST Client:

1. Execute 01 e 02 para obter tokens ADMIN e USER.
2. Execute 03–20 para consultas, validações e bloqueios de segurança.
3. Opcionalmente, execute 21–26 na ordem para testar um imóvel temporário e excluí-lo ao final. Esses passos alteram o banco.

O login de aula usa credenciais fixas: `admin` / `1234` e `usuario` / `1234`. Esses logins não vêm da tabela de usuários e devem ser substituídos por autenticação real nas próximas etapas.

Resultados esperados incluem 200 para consultas autorizadas, 201 para criação de imóvel, 204 para exclusão, 400 para solicitação inválida, 401 para ausência de autenticação ou token inválido, 403 para perfil sem permissão e 404 para imóvel inexistente.

Tokens expiram em uma hora. Execute os logins novamente quando necessário. Upload de fotos e envio real de email não foram verificados no primeiro checkpoint.

## Verificar o frontend

```bash
cd imobiliaria-front
npx tsc --noEmit
npm run build
```

O TypeScript foi verificado na primeira integração. O build de produção é uma verificação separada; as fontes Google utilizadas pelo layout podem exigir internet durante a compilação.

## Problemas comuns

- **JWT_SECRET ausente:** use `./iniciar.sh`, que carrega a chave automaticamente.
- **Erro de conexão com MySQL:** confira o serviço MySQL, o banco, o usuário e a senha em `application.properties`.
- **Frontend não carrega imóveis:** confira se o Spring está rodando na porta 8080 e se sua API responde em `/imoveis`.
- **Nenhum imóvel na vitrine:** confira se existem registros com status disponível; a tela aceita a grafia `Disponível` e `DISPONIVEL`.
- **Porta 3000 ocupada:** confira a URL exibida pelo Next no terminal.

## Arquivos locais

Não enviar ao GitHub senhas, chaves, `application.properties`, `.env`, `.local/`, uploads, `node_modules/`, `.next/` e `target/`. O `.gitignore` já exclui esses arquivos. A configuração de exemplo pode ser versionada.

O esquema completo previsto inclui ofertas, contratos e favoritos, ainda pendentes de implementação.
