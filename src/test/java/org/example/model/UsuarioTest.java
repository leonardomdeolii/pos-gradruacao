package org.example.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UsuarioTest {

    @Test
    void construtorVazio_criaInstanciaComCamposNulos() {
        Usuario u = new Usuario();

        assertNull(u.getId());
        assertNull(u.getNome());
        assertNull(u.getEmail());
        assertNull(u.getCriadoEm());
    }

    @Test
    void construtorNomeEmail_preencheApenasNomeEEmail() {
        Usuario u = new Usuario("Fulano", "fulano@example.com");

        assertNull(u.getId());
        assertEquals("Fulano", u.getNome());
        assertEquals("fulano@example.com", u.getEmail());
        assertNull(u.getCriadoEm());
    }

    @Test
    void construtorCompleto_preencheTodosOsCampos() {
        LocalDateTime agora = LocalDateTime.of(2026, 1, 1, 12, 0);
        Usuario u = new Usuario(10L, "Ciclano", "ciclano@example.com", agora);

        assertEquals(10L, u.getId());
        assertEquals("Ciclano", u.getNome());
        assertEquals("ciclano@example.com", u.getEmail());
        assertEquals(agora, u.getCriadoEm());
    }

    @Test
    void setters_atualizamCampos() {
        Usuario u = new Usuario();
        LocalDateTime agora = LocalDateTime.now();

        u.setId(1L);
        u.setNome("Novo");
        u.setEmail("novo@example.com");
        u.setCriadoEm(agora);

        assertEquals(1L, u.getId());
        assertEquals("Novo", u.getNome());
        assertEquals("novo@example.com", u.getEmail());
        assertEquals(agora, u.getCriadoEm());
    }

    @Test
    void toString_contemCamposPrincipais() {
        Usuario u = new Usuario(1L, "Fulano", "fulano@example.com", LocalDateTime.now());

        String s = u.toString();

        assertNotNull(s);
        org.junit.jupiter.api.Assertions.assertTrue(s.contains("Fulano"));
        org.junit.jupiter.api.Assertions.assertTrue(s.contains("fulano@example.com"));
    }
}
