package org.example.controller;

import org.example.model.Usuario;
import org.example.repository.UsuarioDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    @Mock
    private UsuarioDAO dao;

    @InjectMocks
    private UsuarioController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void criar_retornaCreatedComLocation() {
        Usuario salvo = new Usuario(1L, "Fulano", "fulano@example.com", LocalDateTime.now());
        when(dao.inserir(any(Usuario.class))).thenReturn(salvo);

        ResponseEntity<Usuario> resposta = controller.criar(
                new UsuarioController.UsuarioRequest("Fulano", "fulano@example.com"));

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1L, resposta.getBody().getId());
        assertEquals("/usuarios/1", resposta.getHeaders().getLocation().toString());
    }

    @Test
    void criar_comNomeVazio_lancaBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.criar(new UsuarioController.UsuarioRequest(" ", "x@x.com")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void criar_comEmailNulo_lancaBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.criar(new UsuarioController.UsuarioRequest("Fulano", null)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void criar_quandoDaoFalha_lancaConflict() {
        when(dao.inserir(any(Usuario.class))).thenThrow(new RuntimeException("unique"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.criar(new UsuarioController.UsuarioRequest("Fulano", "dup@example.com")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void listar_retornaListaDoDao() {
        List<Usuario> esperado = List.of(
                new Usuario(1L, "A", "a@a.com", LocalDateTime.now()),
                new Usuario(2L, "B", "b@b.com", LocalDateTime.now())
        );
        when(dao.listarTodos()).thenReturn(esperado);

        List<Usuario> resultado = controller.listar();

        assertEquals(2, resultado.size());
        assertEquals("A", resultado.get(0).getNome());
    }

    @Test
    void buscar_existente_retornaUsuario() {
        Usuario u = new Usuario(1L, "Fulano", "fulano@example.com", LocalDateTime.now());
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(u));

        Usuario resultado = controller.buscar(1L);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscar_inexistente_lancaNotFound() {
        when(dao.buscarPorId(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.buscar(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void atualizar_existente_retornaAtualizado() {
        Usuario existente = new Usuario(1L, "Velho", "velho@example.com", LocalDateTime.now());
        when(dao.buscarPorId(1L)).thenReturn(Optional.of(existente));

        Usuario resultado = controller.atualizar(1L,
                new UsuarioController.UsuarioRequest("Novo", "novo@example.com"));

        assertEquals("Novo", resultado.getNome());
        assertEquals("novo@example.com", resultado.getEmail());
        verify(dao).atualizar(existente);
    }

    @Test
    void atualizar_inexistente_lancaNotFound() {
        when(dao.buscarPorId(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.atualizar(99L,
                        new UsuarioController.UsuarioRequest("X", "x@x.com")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deletar_existente_retornaNoContent() {
        when(dao.deletar(1L)).thenReturn(true);

        ResponseEntity<Void> resposta = controller.deletar(1L);

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
    }

    @Test
    void deletar_inexistente_lancaNotFound() {
        when(dao.deletar(anyLong())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.deletar(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
