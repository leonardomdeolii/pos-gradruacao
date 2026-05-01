package org.example.repository;

import org.example.config.DatabaseConnection;
import org.example.model.Usuario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * Testes do {@link UsuarioDAO} usando H2 em memória.
 *
 * A conexão estática de {@link DatabaseConnection#get()} é interceptada com
 * {@code mockStatic} para apontar para um banco isolado em memória por execução.
 */
class UsuarioDAOTest {

    private static final String TEST_URL =
            "jdbc:h2:mem:usuariodao;DB_CLOSE_DELAY=-1;MODE=LEGACY";

    private static MockedStatic<DatabaseConnection> mocked;
    private UsuarioDAO dao;

    @BeforeAll
    static void installMock() {
        mocked = mockStatic(DatabaseConnection.class);
        mocked.when(DatabaseConnection::get)
                .thenAnswer(invocation -> DriverManager.getConnection(TEST_URL, "sa", ""));
    }

    @AfterAll
    static void removeMock() {
        mocked.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        dao = new UsuarioDAO();
        dao.criarTabela();
    }

    @AfterEach
    void limparTabela() throws SQLException {
        try (Connection c = DriverManager.getConnection(TEST_URL, "sa", "");
             Statement s = c.createStatement()) {
            s.execute("DROP TABLE IF EXISTS usuario");
        }
    }

    @Test
    void inserir_persisteUsuarioEGeraId() {
        Usuario salvo = dao.inserir(new Usuario("Fulano", "fulano@example.com"));

        assertNotNull(salvo.getId());
        assertEquals("Fulano", salvo.getNome());
        assertEquals("fulano@example.com", salvo.getEmail());
        assertNotNull(salvo.getCriadoEm());
    }

    @Test
    void inserir_comEmailDuplicado_lancaRuntimeException() {
        dao.inserir(new Usuario("Fulano", "dup@example.com"));

        assertThrows(RuntimeException.class,
                () -> dao.inserir(new Usuario("Outro", "dup@example.com")));
    }

    @Test
    void buscarPorId_existente_retornaUsuario() {
        Usuario salvo = dao.inserir(new Usuario("Fulano", "fulano@example.com"));

        Optional<Usuario> encontrado = dao.buscarPorId(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(salvo.getId(), encontrado.get().getId());
    }

    @Test
    void buscarPorId_inexistente_retornaEmpty() {
        Optional<Usuario> encontrado = dao.buscarPorId(9999L);

        assertTrue(encontrado.isEmpty());
    }

    @Test
    void listarTodos_retornaOrdenadoPorId() {
        dao.inserir(new Usuario("A", "a@example.com"));
        dao.inserir(new Usuario("B", "b@example.com"));
        dao.inserir(new Usuario("C", "c@example.com"));

        List<Usuario> todos = dao.listarTodos();

        assertEquals(3, todos.size());
        assertEquals("A", todos.get(0).getNome());
        assertEquals("C", todos.get(2).getNome());
    }

    @Test
    void atualizar_existente_retornaTrueEPersisteNovosValores() {
        Usuario salvo = dao.inserir(new Usuario("Velho", "velho@example.com"));
        salvo.setNome("Novo");
        salvo.setEmail("novo@example.com");

        boolean atualizou = dao.atualizar(salvo);

        assertTrue(atualizou);
        Usuario recarregado = dao.buscarPorId(salvo.getId()).orElseThrow();
        assertEquals("Novo", recarregado.getNome());
        assertEquals("novo@example.com", recarregado.getEmail());
    }

    @Test
    void atualizar_inexistente_retornaFalse() {
        Usuario fantasma = new Usuario(9999L, "X", "x@example.com", null);

        boolean atualizou = dao.atualizar(fantasma);

        assertFalse(atualizou);
    }

    @Test
    void deletar_existente_retornaTrueERemove() {
        Usuario salvo = dao.inserir(new Usuario("Fulano", "fulano@example.com"));

        boolean removeu = dao.deletar(salvo.getId());

        assertTrue(removeu);
        assertTrue(dao.buscarPorId(salvo.getId()).isEmpty());
    }

    @Test
    void deletar_inexistente_retornaFalse() {
        boolean removeu = dao.deletar(9999L);

        assertFalse(removeu);
    }
}
