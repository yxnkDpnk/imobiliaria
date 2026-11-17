# Verificações da Aula 3

Verificação realizada em 17/11/2026, com MySQL local e os dois servidores em execução.

## Backend

- `./mvnw test`: 33 testes, sem falhas ou erros.
- 72 requisições de integração: compra, aluguel, favoritos, filtros por situação, aprovação, envio, assinatura, cancelamento, propriedade dos dados, preservação do PDF e permissões.
- Duas ofertas simultâneas do mesmo cliente: somente uma foi cadastrada; a outra recebeu 409.
- Dois aceites simultâneos para o mesmo imóvel: somente um foi aceito; o outro recebeu 409.
- A assinatura concluiu a venda ou o aluguel e recusou as propostas concorrentes.
- O cancelamento liberou o imóvel e reativou as propostas em espera.
- O PDF foi baixado com autenticação e teve o conteúdo lido por um leitor de PDF.
- O PDF é salvo no banco e preserva os dados combinados na preparação, mesmo após editar o cadastro do imóvel ou do cliente.
- A notificação de contrato possui testes para envio bem-sucedido e falha do serviço de email, com remetente simulado. Nenhum email externo foi enviado durante as verificações.

## Frontend

- TypeScript, ESLint e build de produção concluídos.
- Fluxo dos três perfis verificado no navegador: proposta → aceite → preparação → aprovação → envio → assinatura.
- 20 verificações do fluxo principal da interface concluídas sem erros de JavaScript, além das verificações dos cadastros e fotos.
- Favoritos, filtros combinados, ordenação e paginação verificados com mais de seis imóveis.
- Validação de login vazio, email inválido e credenciais incorretas.
- Sem token, a rota protegida redirecionou ao login. Cliente não acessou a área administrativa.
- Erro 409 mostrou a regra de negócio; 404 mostrou imóvel não encontrado.
- Erro 401 apagou a sessão e mostrou o aviso de expiração no login.
- Falha de servidor mostrou mensagem amigável e Tentar novamente recuperou os dados.
- Cadastros de bairros, tipos e usuários criados, editados e excluídos pela interface.
- Imóvel cadastrado, editado e excluído pela interface.
- Foto PNG enviada, consultada pela URL pública e excluída pela interface.
- O botão de excluir a própria conta administrativa ficou bloqueado.
- Vitrine verificada em largura de celular sem rolagem horizontal.

Os registros temporários usados nas verificações foram removidos. O banco da entrega mantém os usuários de demonstração e os imóveis de exemplo.
