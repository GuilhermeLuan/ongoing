package dev.guilhermeluan.ongoing.messages.services;

import dev.guilhermeluan.ongoing.messages.entities.Mensagem;
import dev.guilhermeluan.ongoing.messages.repositories.MensagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;

    @Autowired
    public MensagemService(MensagemRepository mensagemRepository) {
        this.mensagemRepository = mensagemRepository;
    }

    public Mensagem criarMensagem(String mensagem) {
        Mensagem mensagem = new Mensagem();
        mensagem.setMsg(mensagem);
        return mensagemRepository.save(mensagem);
    }

    public List<Mensagem> listarMensagens() {
        return mensagemRepository.findAll();
    }

    public Mensagem buscarMensagemPorId(Long id) {
        return mensagemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada com ID: " + id));
    }
}
