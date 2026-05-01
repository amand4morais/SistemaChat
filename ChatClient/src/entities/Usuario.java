package entities;

public class Usuario {
	
	private String usuario;
	private String nome;
	private String senha;
	private String token;
	
	public Usuario() {
		
	}
	
	public Usuario(String usuario, String nome, String senha) {
		this.usuario = usuario;
		this.nome = nome;
		this.senha = senha;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
