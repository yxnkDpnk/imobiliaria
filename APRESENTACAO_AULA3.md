# Apresentação — Aula 3

## Antes de começar

Inicie o MySQL e mantenha dois terminais abertos:

```bash
# Terminal 1, a partir da pasta do repositório
cd primeiro-projeto
./iniciar.sh
```

```bash
# Terminal 2, a partir da pasta do repositório
cd imobiliaria-front
npm run dev
```

Site: http://localhost:3000. No VS Code, Ctrl + clique abre os links do arquivo `aula3.http`.

## Fluxo principal pela interface

Todos os acessos abaixo usam a senha `1234`. Clique em **Sair** antes de trocar de perfil.

1. **Cliente — cliente@imobiliaria.com**
   - Na vitrine, escolha um imóvel disponível e clique em Ver detalhes.
   - Salve nos favoritos.
   - Em Fazer oferta, escolha Compra ou Aluguel, preencha um valor e uma mensagem e envie.
   - Abra Minha área e mostre a oferta pendente e a lista de favoritos.
2. **Corretor — corretor@imobiliaria.com**
   - Na aba Ofertas, aceite a proposta. O imóvel fica reservado.
   - Clique em Preparar contrato, escreva as observações e clique em Gerar contrato.
   - Abra Contratos e mostre que o documento está em aprovação.
3. **Administrador — admin@imobiliaria.com**
   - Na aba Contratos, baixe o PDF e confira os dados.
   - Clique em Aprovar contrato e depois em Enviar ao cliente.
   - Mostre a data do envio e o estado da notificação por email.
4. **Cliente**
   - Em Minha área → Meus contratos, baixe o PDF.
   - Clique em Assinar contrato, confira o nome, marque o aceite e confirme.
   - Mostre o contrato assinado e a data. O imóvel fica vendido ou alugado.
5. **Administrador**
   - Mostre o Painel com totais e os três gráficos.
   - Em Cadastros, mostre as abas de imóveis, bairros, tipos, usuários e fotos.
   - Se quiser repetir a demonstração, cancele o contrato na aba Contratos. O imóvel ficará disponível para uma nova oferta.

Se estiver usando janelas diferentes para cada perfil, clique em Atualizar depois de cada mudança.

## Regras para demonstrar

- Envie a mesma oferta duas vezes: o segundo envio mostra a mensagem de oferta pendente duplicada.
- Aceite uma oferta: o botão de nova oferta desaparece do detalhe ao recarregar.
- Faça ofertas com dois clientes antes de aceitar uma: a outra fica em espera.
- Sem login, tente abrir http://localhost:3000/admin: o site pede login.
- Como corretor, abra `/admin`: o sistema retorna para a área do corretor.
- Tente assinar antes da aprovação e do envio pelo arquivo HTTP: a API bloqueia.

## Testes HTTP

O arquivo [primeiro-projeto/aula3.http](primeiro-projeto/aula3.http) cria um imóvel próprio para demonstração e percorre o fluxo completo. Os itens que esperam 400, 403 ou 409 são verificações de bloqueio, portanto essas respostas são o resultado correto.

O contrato em PDF e a assinatura são uma simulação acadêmica. Para notificações externas, o SMTP e a opção `app.notificacoes-email=true` precisam estar configurados; o painel mostra se a notificação foi enviada, falhou ou está pendente.
