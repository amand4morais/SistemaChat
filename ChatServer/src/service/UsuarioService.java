package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import dao.UsuarioDAO;
import entities.Usuario;

public class UsuarioService {

    private UsuarioDAO dao;

    private static final Set<String> tokensAtivos =
            Collections.synchronizedSet(new HashSet<>());

    public UsuarioService(Connection conn) {
        this.dao = new UsuarioDAO(conn);
    }

    public boolean cadastrar(Usuario usuario) throws SQLException {
        if (!validarNome(usuario.getNome()) || !validarUsuario(usuario.getUsuario()) || !validarSenha(usuario.getSenha())) {
            return false;
        }

        int linhasAfetadas = dao.cadastrarUsuario(usuario);
        return linhasAfetadas > 0;
    }

    public Usuario login(String usuario, String senha) throws SQLException {
        if (!validarUsuario(usuario) || !validarSenha(senha)) {
            return null;
        }

        Usuario u = dao.buscarPorUsuarioESenha(usuario, senha);

        if (u == null) {
            return null;
        }

        String token = gerarToken(u.getUsuario());
        u.setToken(token);
        tokensAtivos.add(token);

        return u;
    }

    public Usuario consultar(String token) throws SQLException {
        if (!tokenEstaAtivo(token)) {
            return null;
        }

        String nomeUsuario = extrairUsuarioDoToken(token);

        if (nomeUsuario == null) {
            return null;
        }

        Usuario u = dao.consultarPorNomeUsuario(nomeUsuario);

        if (u != null) {
            u.setToken(token);
        }

        return u;
    }

    public boolean atualizar(String token, Usuario novosDados) throws SQLException {
        if (!tokenEstaAtivo(token)) {
            return false;
        }

        String nomeUsuario = extrairUsuarioDoToken(token);

        if (nomeUsuario == null) {
            return false;
        }

        Usuario existente = dao.consultarPorNomeUsuario(nomeUsuario);

        if (existente == null) {
            return false;
        }

        if (!validarNome(novosDados.getNome()) || !validarSenha(novosDados.getSenha())) {
            return false;
        }

        int linhasAfetadas = dao.atualizarUsuario(nomeUsuario, novosDados);
        return linhasAfetadas > 0;
    }

    public boolean deletar(String token) throws SQLException {
        if (!tokenEstaAtivo(token)) {
            return false;
        }

        String nomeUsuario = extrairUsuarioDoToken(token);

        if (nomeUsuario == null) {
            return false;
        }

        Usuario existente = dao.consultarPorNomeUsuario(nomeUsuario);

        if (existente == null) {
            return false;
        }

        int linhasAfetadas = dao.deletarUsuario(nomeUsuario);

        if (linhasAfetadas > 0) {
            tokensAtivos.remove(token);
            return true;
        }

        return false;
    }

    public boolean logout(String token) {
        if (!tokenEstaAtivo(token)) {
            return false;
        }
        return tokensAtivos.remove(token);
    }

    private boolean tokenEstaAtivo(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return tokensAtivos.contains(token);
    }

    private boolean validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean validarUsuario(String login) {
        if (login == null) {
            return false;
        }
        return login.matches("^[a-zA-Z0-9]{5,20}$");
    }

    private boolean validarSenha(String senha) {
        if (senha == null || senha.length() != 6) {
            return false;
        }
        return senha.matches("\\d+");
    }

    private String extrairUsuarioDoToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        if (token.equals("adm")) {
            return "admin";
        }

        String[] partes = token.split("_");

        if (partes.length != 2) {
            return null;
        }

        if (!partes[0].equals("usr")) {
            return null;
        }

        if (!partes[1].matches("^[a-zA-Z0-9]{5,20}$")) {
            return null;
        }

        return partes[1];
    }

    private String gerarToken(String nomeUsuario) {
        return "usr_" + nomeUsuario;
    }
}