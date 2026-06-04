package dev.guilhermeluan.ongoing.messages.controllers;

import dev.guilhermeluan.ongoing.messages.entities.Mensagem;
import dev.guilhermeluan.ongoing.messages.services.MensagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MensagemController {

    private final MensagemService mensagemService;

    @Autowired
    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @PostMapping
    public ResponseEntity<Mensagem> criarMensagem(@RequestBody String msg) {
        Mensagem mensagem = mensagemService.criarMensagem(msg);
        return new ResponseEntity<>(mensagem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Mensagem>> listarMensagens() {
        List<Mensagem> mensagens = mensagemService.listarMensagens();
        return ResponseEntity.ok(mensagens);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mensagem> buscarMensagemPorId(@PathVariable Long id) {
        Mensagem mensagem = mensagemService.buscarMensagemPorId(id);
        return ResponseEntity.ok(mensagem);
    }
}
