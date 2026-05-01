package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ClientePrincipal {

    private static String token = null;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Gson gson = new Gson();

        System.out.print("Digite o IP do servidor: ");
        String ip = scanner.nextLine();

        System.out.print("Digite a porta do servidor: ");
        int porta = lerInteiroSeguro(scanner);

        Socket socket = null;
        BufferedReader entrada = null;
        PrintWriter saida = null;

        try {
            socket = new Socket(ip, porta);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            saida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("Conectado ao servidor " + ip + ":" + porta);

            boolean encerrar = false;

            while (!encerrar) {
                if (token == null) {
                    encerrar = menuDeslogado(scanner, gson, entrada, saida);
                } else {
                    menuLogado(scanner, gson, entrada, saida);
                }
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar com o servidor: " + e.getMessage());
        } finally {
            try {
                if (entrada != null) entrada.close();
                if (saida != null)   saida.close();
                if (socket != null)  socket.close();
            } catch (IOException e) {
                System.out.println("Erro ao encerrar conexao: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static boolean menuDeslogado(Scanner scanner, Gson gson, BufferedReader entrada, PrintWriter saida) throws IOException {

        System.out.println("\n=== MENU ===");
        System.out.println("1 - Cadastrar usuario");
        System.out.println("2 - Login");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opcao: ");

        int opcao = lerInteiroSeguro(scanner);

        switch (opcao) {

        case 1: {
            System.out.print("Nome: ");
            String nome = scanner.nextLine();

            System.out.print("Usuario (5 - 20 caracteres): ");
            String usuario = scanner.nextLine();

            System.out.print("Senha (numerica, 6 digitos): ");
            String senha = scanner.nextLine();

            JsonObject json = new JsonObject();
            json.addProperty("op", "cadastrarUsuario");
            json.addProperty("nome", nome);
            json.addProperty("usuario", usuario);
            json.addProperty("senha", senha);

            String resposta = enviar(entrada, saida, json.toString());

            if (resposta != null) {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                String codigo = jsonResposta.get("resposta").getAsString();

                if (codigo.equals("200")) {
                    System.out.println("Cadastro realizado com sucesso!");
                } else {
                    System.out.println("Cadastro falhou. Verifique se:");
                    System.out.println("  - O nome de usuario tem entre 5 e 20 caracteres sem espacos");
                    System.out.println("  - A senha tem exatamente 6 digitos numericos");
                    System.out.println("  - O nome de usuario nao esta em uso por outra conta");
                }
            }
            break;
        }

        case 2: {
            System.out.print("Usuario: ");
            String usuario = scanner.nextLine();

            System.out.print("Senha: ");
            String senha = scanner.nextLine();

            JsonObject json = new JsonObject();
            json.addProperty("op", "login");
            json.addProperty("usuario", usuario);
            json.addProperty("senha", senha);

            String resposta = enviar(entrada, saida, json.toString());

            if (resposta != null) {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                String codigo = jsonResposta.get("resposta").getAsString();

                if (codigo.equals("200")) {
                    token = jsonResposta.get("token").getAsString();
                    System.out.println("Login realizado com sucesso!");
                } else {
                    System.out.println("Login falhou. Verifique se o usuario e a senha estao corretos.");
                }
            }
            break;
        }

        case 0: {
            System.out.println("Encerrando cliente.");
            return true;
        }

        default: {
            System.out.println("Opcao invalida.");
            break;
        }
        }

        return false;
    }

    private static void menuLogado(Scanner scanner, Gson gson, BufferedReader entrada, PrintWriter saida) throws IOException {

        System.out.println("\n=== MENU ===");
        System.out.println("1 - Consultar dados");
        System.out.println("2 - Atualizar cadastro");
        System.out.println("3 - Excluir conta");
        System.out.println("0 - Logout");
        System.out.print("Escolha uma opcao: ");

        int opcao = lerInteiroSeguro(scanner);

        switch (opcao) {

        case 1: {
            JsonObject json = new JsonObject();
            json.addProperty("op", "consultarUsuario");
            json.addProperty("token", token);

            String resposta = enviar(entrada, saida, json.toString());

            if (resposta != null) {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                String codigo = jsonResposta.get("resposta").getAsString();

                if (codigo.equals("200")) {
                    System.out.println("Dados do usuario:");
                    System.out.println("  Nome: " + jsonResposta.get("nome").getAsString());
                    System.out.println("  Usuario: " + jsonResposta.get("usuario").getAsString());
                } else {
                    System.out.println("Consulta falhou. Nao foi possivel encontrar os dados do usuario.");
                }
            }
            break;
        }

        case 2: {
            System.out.print("Novo nome: ");
            String nome = scanner.nextLine();

            System.out.print("Nova senha: ");
            String senha = scanner.nextLine();

            JsonObject json = new JsonObject();
            json.addProperty("op", "atualizarUsuario");
            json.addProperty("token", token);
            json.addProperty("nome", nome);
            json.addProperty("senha", senha);

            String resposta = enviar(entrada, saida, json.toString());

            if (resposta != null) {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                String codigo = jsonResposta.get("resposta").getAsString();

                if (codigo.equals("200")) {
                    System.out.println("Dados atualizados com sucesso!");
                } else {
                    System.out.println("Atualizacao falhou. Verifique se:");
                    System.out.println("  - A nova senha tem exatamente 6 digitos numericos");
                }
            }
            break;
        }

        case 3: {
            JsonObject json = new JsonObject();
            json.addProperty("op", "deletarUsuario");
            json.addProperty("token", token);

            String resposta = enviar(entrada, saida, json.toString());

            if (resposta != null) {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                String codigo = jsonResposta.get("resposta").getAsString();

                if (codigo.equals("200")) {
                    token = null;
                    System.out.println("Conta excluida com sucesso. Voce foi desconectado.");
                } else {
                    System.out.println("Exclusao falhou. Nao foi possivel deletar a conta.");
                }
            }
            break;
        }

        case 0: {
            JsonObject json = new JsonObject();
            json.addProperty("op", "logout");
            json.addProperty("token", token);

            String resposta = enviar(entrada, saida, json.toString());

            if (resposta != null) {
                JsonObject jsonResposta = gson.fromJson(resposta, JsonObject.class);
                String codigo = jsonResposta.get("resposta").getAsString();

                if (codigo.equals("200")) {
                    token = null;
                    System.out.println("Logout realizado com sucesso.");
                } else {
                    System.out.println("Falha ao realizar logout. Tente novamente.");
                }
            } else {
                System.out.println("Sem resposta do servidor. Logout nao confirmado.");
            }
            break;
        }

        default: {
            System.out.println("Opcao invalida.");
            break;
        }
        }
    }

    private static String enviar(BufferedReader entrada, PrintWriter saida, String jsonEnviado) throws IOException {
        System.out.println("\nJSON enviado: " + jsonEnviado);
        saida.println(jsonEnviado);

        String jsonRecebido = entrada.readLine();
        System.out.println("JSON recebido: " + jsonRecebido);

        return jsonRecebido;
    }

    private static int lerInteiroSeguro(Scanner scanner) {
        while (true) {
            try {
                int valor = scanner.nextInt();
                scanner.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                scanner.nextLine();
                System.out.print("Entrada invalida. Digite apenas numeros: ");
            }
        }
    }
}