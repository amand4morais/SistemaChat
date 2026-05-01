package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import entities.Usuario;

public class UsuarioDAO {

	private Connection conn;

	public UsuarioDAO(Connection conn) {
		super();
		this.conn = conn;
	}

	public int cadastrarUsuario(Usuario usuario) throws SQLException {
		PreparedStatement st = null;

		try {
			st = conn.prepareStatement("INSERT INTO usuarios (usuario, nome, senha) VALUES (?, ?, ?)");

			st.setString(1, usuario.getUsuario());
			st.setString(2, usuario.getNome());
			st.setString(3, usuario.getSenha());

			return st.executeUpdate();
		} finally {
			BancoDados.finalizarStatement(st);
		}
	}

	public Usuario buscarPorUsuarioESenha(String usuario, String senha) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT * FROM usuarios WHERE usuario = ? AND senha = ?");

			st.setString(1, usuario);
			st.setString(2, senha);

			rs = st.executeQuery();

			if (rs.next()) {
				Usuario u = new Usuario();
				u.setUsuario(rs.getString("usuario"));
				u.setNome(rs.getString("nome"));
				u.setSenha(rs.getString("senha"));
				return u;
			}

			return null;
		} finally {
			BancoDados.finalizarResultSet(rs);
			BancoDados.finalizarStatement(st);
		}
	}

	public Usuario consultarPorNomeUsuario(String nomeUsuario) throws SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = conn.prepareStatement("SELECT * FROM usuarios WHERE usuario = ?");

			st.setString(1, nomeUsuario);

			rs = st.executeQuery();

			if (rs.next()) {
				Usuario u = new Usuario();
				u.setUsuario(rs.getString("usuario"));
				u.setNome(rs.getString("nome"));
				u.setSenha(rs.getString("senha"));
				return u;
			}

			return null;
		} finally {
			BancoDados.finalizarResultSet(rs);
			BancoDados.finalizarStatement(st);
		}
	}

	public int atualizarUsuario(String nomeUsuario, Usuario usuario) throws SQLException {
		PreparedStatement st = null;

		try {
			st = conn.prepareStatement("UPDATE usuarios SET nome = ?, senha = ? WHERE usuario = ?");

			st.setString(1, usuario.getNome());
			st.setString(2, usuario.getSenha());
			st.setString(3, nomeUsuario);

			return st.executeUpdate();
		} finally {
			BancoDados.finalizarStatement(st);
		}
	}

	public int deletarUsuario(String nomeUsuario) throws SQLException {
		PreparedStatement st = null;

		try {
			st = conn.prepareStatement("DELETE FROM usuarios WHERE usuario = ?");

			st.setString(1, nomeUsuario);

			return st.executeUpdate();
		} finally {
			BancoDados.finalizarStatement(st);
		}
	}
}