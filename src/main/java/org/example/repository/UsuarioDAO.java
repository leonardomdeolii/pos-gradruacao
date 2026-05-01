package org.example.repository;

import org.example.config.DatabaseConnection;
import org.example.model.Usuario;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) responsável pelas operações de persistência
 * da entidade {@link Usuario} no banco H2.
 */
@Repository
public class UsuarioDAO {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS usuario (
                id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                nome       VARCHAR(150) NOT NULL,
                email      VARCHAR(150) NOT NULL UNIQUE,
                criado_em  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    /** Garante que a tabela exista assim que o bean é criado. */
    @PostConstruct
    public void init() {
        criarTabela();
    }

    /** Garante que a tabela exista. Chame uma vez no início da aplicação. */
    public void criarTabela() {
        try (Connection conn = DatabaseConnection.get();
             Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar tabela usuario", e);
        }
    }

    public Usuario inserir(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getLong(1));
                }
            }
            return buscarPorId(usuario.getId())
                    .orElseThrow(() -> new SQLException("Falha ao recuperar usuário inserido"));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário", e);
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT id, nome, email, criado_em FROM usuario WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por id", e);
        }
    }

    public List<Usuario> listarTodos() {
        String sql = "SELECT id, nome, email, criado_em FROM usuario ORDER BY id";
        List<Usuario> resultado = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                resultado.add(map(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários", e);
        }
    }

    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setLong(3, usuario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário", e);
        }
    }

    public boolean deletar(Long id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar usuário", e);
        }
    }

    private Usuario map(ResultSet rs) throws SQLException {
        Timestamp criadoEm = rs.getTimestamp("criado_em");
        return new Usuario(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("email"),
                criadoEm != null ? criadoEm.toLocalDateTime() : null
        );
    }
}
