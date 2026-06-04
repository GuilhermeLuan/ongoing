package dev.guilhermeluan.ongoing.messages.repositories;

import dev.guilhermeluan.ongoing.messages.entities.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {
}
