package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dao.BancoDados;
import entities.Usuario;
import service.UsuarioService;

public class SocketThread extends Thread {

    private Socket socket;
    private Gson gson;

    public SocketThread(Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
    }

    @Override
    public void run() {

        BufferedReader entrada = null;
        PrintWriter saida = null;
        Connection conn = null;

        try {
            conn = BancoDados.conectar();
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
            fecharSocket();
            return;
        } catch (IOException e) {
            System.out.println("Erro ao carregar configuracoes do banco: " + e.getMessage());
            fecharSocket();
            return;
        }

        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            Type tipoMap = new TypeToken<Map<String, String>>() {}.getType();
            String jsonRecebido;

            while ((jsonRecebido = entrada.readLine()) != null) {
                System.out.println("JSON recebido: " + jsonRecebido);

                String jsonResposta;

                try {
                    Map<String, String> dados = gson.fromJson(jsonRecebido, tipoMap);
                    UsuarioService service = new UsuarioService(conn);
                    jsonResposta = processarOperacao(dados, service);
                } catch (Exception e) {
                    jsonResposta = montarResposta("401", null, null, "Erro interno ou JSON malformado");
                }

                System.out.println("JSON enviado: " + jsonResposta);
                saida.println(jsonResposta);
            }

        } catch (IOException e) {
            System.out.println("Erro de comunicacao com o cliente: " + socket.getInetAddress().getHostAddress());
        } finally {
            try {
                if (entrada != null) entrada.close();
                if (saida != null)   saida.close();
                if (socket != null)  socket.close();
                if (conn != null)    conn.close();
            } catch (IOException | SQLException e) {
                System.out.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    private String processarOperacao(Map<String, String> dados, UsuarioService service) {

        String op = dados.get("op");

        if (op == null) {
            return montarResposta("401", null, null, "Operacao nao informada");
        }

        try {

            switch (op) {

            case "cadastrarUsuario": {
                Usuario usuario = new Usuario();
                usuario.setUsuario(dados.get("usuario"));
                usuario.setNome(dados.get("nome"));
                usuario.setSenha(dados.get("senha"));

                boolean sucesso = service.cadastrar(usuario);

                if (sucesso) {
                    return montarResposta("200", null, null, "Cadastrado com sucesso");
                }
                return montarResposta("401", null, null, "Dados invalidos. Verifique campos vazios, tamanho do login (5-20) ou se usuario ja existe.");
            }

            case "login": {
                String usuario = dados.get("usuario");
                String senha = dados.get("senha");

                Usuario u = service.login(usuario, senha);

                if (u != null) {
                    return montarResposta("200", null, u.getToken(), null);
                }
                return montarResposta("401", null, null, "Usuário ou senha inválidos");
            }

            case "consultarUsuario": {
                String token = dados.get("token");

                Usuario u = service.consultar(token);

                if (u != null) {
                    return montarResposta("200", u, null, null);
                }
                return montarResposta("401", null, null, "Token inválido ou sessão encerrada");
            }

            case "atualizarUsuario": {
                String token = dados.get("token");

                Usuario novosDados = new Usuario();
                novosDados.setNome(dados.get("nome"));
                novosDados.setSenha(dados.get("senha"));

                boolean sucesso = service.atualizar(token, novosDados);

                if (sucesso) {
                    return montarResposta("200", null, null, "Atualizado com sucesso");
                }
                return montarResposta("401", null, null, "Falha na atualizacao. Verifique se a senha possui 6 digitos numéricos e campos preenchidos.");
            }

            case "deletarUsuario": {
                String token = dados.get("token");

                boolean sucesso = service.deletar(token);

                if (sucesso) {
                    return montarResposta("200", null, null, "Deletado com sucesso");
                }
                return montarResposta("401", null, null, "Token inválido ou sessão encerrada");
            }

            case "logout": {
                String token = dados.get("token");

                boolean sucesso = service.logout(token);

                if (sucesso) {
                    return montarResposta("200", null, null, "logout efetuado");
                }
                return montarResposta("401", null, null, "Erro ao efetuar logout. Token invalido ou sessao ja encerrada.");
            }

            default:
                return montarResposta("401", null, null, "Operacao desconhecida");
            }

        } catch (SQLException e) {
            System.out.println("Erro no banco de dados: " + e.getMessage());
            return montarResposta("401", null, null, "Erro de banco de dados ou usuario ja existente");
        }
    }

    private String montarResposta(String codigo, Usuario usuario, String token, String mensagem) {
        java.util.HashMap<String, String> resposta = new java.util.HashMap<>();
        resposta.put("resposta", codigo);

        if (usuario != null) {
            resposta.put("usuario", usuario.getUsuario());
            resposta.put("nome", usuario.getNome());
        }

        if (token != null) {
            resposta.put("token", token);
        }

        if (mensagem != null) {
            resposta.put("mensagem", mensagem);
        }

        return gson.toJson(resposta);
    }

    private void fecharSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar socket: " + e.getMessage());
        }
    }
}