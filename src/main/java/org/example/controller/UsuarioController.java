package org.example.controller;

import org.example.model.Usuario;
import org.example.repository.UsuarioDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

/**
 * Controller REST para o cadastro de usu?rios.
 *
 * Endpoints:
 *   POST   /usuarios          -> cria um novo usu?rio
 *   GET    /usuarios          -> lista todos
 *   GET    /usuarios/{id}     -> busca por id
 *   PUT    /usuarios/{id}     -> atualiza
 *   DELETE /usuarios/{id}     -> remove
 */
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioDAO dao;

    public UsuarioController(UsuarioDAO dao) {
        this.dao = dao;
    }

    /** DTO simples usado no corpo das requisi??es. */
    public record UsuarioRequest(String nome, String email) {}

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody UsuarioRequest req) {
        validar(req);
        try {
            Usuario salvo = dao.inserir(new Usuario(req.nome(), req.email()));
            return ResponseEntity.created(URI.create("/usuarios/" + salvo.getId())).body(salvo);
        } catch (RuntimeException e) {
            // Provavelmente viola??o de UNIQUE no email
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @GetMapping
    public List<Usuario> listar() {
        return dao.listarTodos();
    }

    @GetMapping("/{id}")
    public Usuario buscar(@PathVariable Long id) {
        return dao.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usu?rio n?o encontrado"));
    }

    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Long id, @RequestBody UsuarioRequest req) {
        validar(req);
        Usuario existente = dao.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usu?rio n?o encontrado"));
        existente.setNome(req.nome());
        existente.setEmail(req.email());
        dao.atualizar(existente);
        return existente;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!dao.deletar(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usu?rio n?o encontrado");
        }
        return ResponseEntity.noContent().build();
    }

    private void validar(UsuarioRequest req) {
        if (req == null || req.nome() == null || req.nome().isBlank()
                || req.email() == null || req.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nome e email s?o obrigat?rios");
        }
    }
}

