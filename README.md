# Sistema Distribuído — Chat Cliente/Servidor
### EP-1 · Sistemas Distribuídos · UTFPR — Campus Ponta Grossa
### Tecnologia em Análise e Desenvolvimento de Software

---

## Visão Geral

Sistema de comunicação Cliente/Servidor desenvolvido em Java, utilizando Sockets TCP para a troca de mensagens no formato JSON. O projeto segue a arquitetura de sistemas distribuídos e implementa persistência de dados via banco de dados MySQL.

### Funcionalidades implementadas na EP-1

- **Conexão Persistente** — o socket é aberto uma única vez e mantido ativo durante toda a sessão do cliente
- **CRUD completo de usuários** — Cadastrar, Consultar, Atualizar e Deletar
- **Sistema de Login/Logout** — com geração e invalidação de tokens de sessão
- **Servidor Multithread** — cada cliente conectado é atendido por uma thread independente
- **Interface via console** — com tratamento de entradas inválidas e mensagens de erro amigáveis
- **Log de tráfego JSON** — todas as mensagens enviadas e recebidas são exibidas no console do cliente e do servidor

---

## Tecnologias Utilizadas

| Tecnologia | Descrição |
|---|---|
| Java SE (JDK 17+) | Linguagem principal |
| Sockets TCP | Comunicação de rede (nativo Java) |
| Gson | Serialização e desserialização de JSON |
| MySQL via XAMPP | Banco de dados relacional |
| Eclipse IDE | Ambiente de desenvolvimento |

---

## Estrutura do Projeto

O sistema é composto por **dois projetos Java independentes**, que devem ser importados e executados separadamente no Eclipse:

```
ChatClient/
└── src/
    ├── entities/
    │   └── Usuario.java
    └── main/
        └── ClientePrincipal.java

ChatServer/
└── src/
    ├── dao/
    │   ├── BancoDados.java
    │   ├── UsuarioDAO.java
    │   └── usuarios.sql          ← script de criação da tabela
    ├── entities/
    │   └── Usuario.java
    ├── main/
    │   ├── ServidorPrincipal.java
    │   └── SocketThread.java
    └── service/
        └── UsuarioService.java
```

---

## Guia de Execução

Siga os passos abaixo na ordem indicada para garantir o funcionamento correto do sistema.

### Passo 1 — Configuração do Banco de Dados (XAMPP)

1. Certifique-se de ter o **XAMPP** instalado na máquina.
2. Abra o **XAMPP Control Panel** e inicie os módulos **Apache** e **MySQL**.
3. Acesse o **phpMyAdmin** pelo navegador em `http://localhost/phpmyadmin`.
4. Crie um novo banco de dados com o nome exato abaixo:

```sql
CREATE DATABASE sistema_chat;
```

5. Selecione o banco `sistema_chat` e execute o script SQL de criação da tabela. O script encontra-se em:

```
ChatServer/src/dao/usuarios.sql
```

6. Verifique se o arquivo `database.properties`, localizado na raiz do projeto `ChatServer`, está configurado corretamente com as credenciais do seu ambiente:

```properties
dburl=jdbc:mysql://localhost:3306/sistema_chat
user=root
password=
```

> **Atenção:** por padrão, o XAMPP utiliza o usuário `root` sem senha. Caso o seu ambiente tenha senha configurada, insira-a no campo `password`.

---

### Passo 2 — Importação dos Projetos no Eclipse

O sistema possui **dois projetos separados** (`ChatClient` e `ChatServer`). Ambos devem ser importados para que apareçam juntos no **Package Explorer**.

Repita o procedimento abaixo **duas vezes** — uma para cada projeto:

1. No Eclipse, acesse **File → Import**.
2. Selecione **General → Existing Projects into Workspace** e clique em **Next**.
3. Em **Select root directory**, clique em **Browse** e navegue até a pasta do projeto (`ChatClient` ou `ChatServer`).
4. Certifique-se de que o projeto aparece marcado na lista e clique em **Finish**.

Ao final, os dois projetos (`ChatClient` e `ChatServer`) devem estar visíveis no **Package Explorer**.

---

### Passo 3 — Configuração das Dependências (Build Path)

Ambos os projetos utilizam bibliotecas externas que devem estar configuradas no **Build Path**:

| Biblioteca | Projeto | Finalidade |
|---|---|---|
| `gson-x.x.x.jar` | Cliente e Servidor | Manipulação de JSON |
| `mysql-connector-j-x.x.x.jar` | Servidor | Conexão com o banco de dados |

Para adicionar cada `.jar` ao projeto:

1. Clique com o botão direito sobre o projeto no **Package Explorer**.
2. Acesse **Build Path → Configure Build Path**.
3. Na aba **Libraries**, clique em **Add External JARs**.
4. Localize o arquivo `.jar` correspondente e clique em **Open**.
5. Clique em **Apply and Close**.

> Os arquivos `.jar` estão disponíveis na pasta `lib/` de cada projeto.

---

### Passo 4 — Execução do Sistema

#### Iniciando o Servidor

1. No **Package Explorer**, expanda o projeto `ChatServer`.
2. Navegue até `src/main/ServidorPrincipal.java`.
3. Clique com o botão direito e selecione **Run As → Java Application**.
4. No console do Eclipse, informe a **porta** que o servidor deve utilizar (ex: `12345`) e pressione **Enter**.
5. O servidor exibirá a mensagem de confirmação e ficará aguardando conexões.

#### Iniciando o Cliente

1. No **Package Explorer**, expanda o projeto `ChatClient`.
2. Navegue até `src/main/ClientePrincipal.java`.
3. Clique com o botão direito e selecione **Run As → Java Application**.
4. No console, informe o **IP** do servidor e a **porta** configurada no passo anterior.
   - Para testar localmente, utilize o IP `127.0.0.1`.
5. Após a conexão ser estabelecida, o menu principal será exibido.

> **Para testar com múltiplos clientes simultaneamente**, repita o passo de execução do cliente abrindo novas instâncias. O servidor atende cada conexão em uma thread independente.

---

## Protocolo de Mensagens

Todas as mensagens trocadas entre cliente e servidor seguem o formato JSON. Exemplos:

**Requisição de login:**
```json
{"op": "login", "usuario": "joao01", "senha": "123456"}
```

**Resposta de sucesso com token:**
```json
{"resposta": "200", "token": "usr_joao01"}
```

**Resposta de erro:**
```json
{"resposta": "401", "mensagem": "Usuário ou senha inválidos"}
```

---

## Roadmap — Próximas Entregas

### EP-2 (Em desenvolvimento)
- Implementação de níveis de acesso: perfil **ADMIN** e perfil **Usuário Comum**
- Operações exclusivas do administrador (gerenciar todos os usuários)
- Início da implementação do sistema de **mensagens diretas 1 para 1** entre usuários

### EP-3 (Planejado)
- **Interface Gráfica (GUI)** completa para cliente e servidor, substituindo a interface de console
- Finalização do sistema de **Chat em tempo real**
- Refinamentos de segurança e estabilidade para entrega final do projeto

---

## Observações Finais

- O código-fonte **não deve ser modificado** durante a avaliação, conforme as regras da disciplina.
- Todos os JSONs enviados e recebidos são exibidos no console em tempo real para fins de depuração e avaliação.
- O servidor suporta **múltiplas conexões simultâneas** sem degradação de desempenho, graças ao modelo multithread implementado.
