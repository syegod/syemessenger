package io.syemessenger.api.messagehistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MessageRepository extends CrudRepository<Message, Long> {

  @NativeQuery("SELECT * FROM messages")
  Page<Message> findAll(Pageable pageable);

  Page<Message> findByMessageContaining(String keyword, Pageable pageable);
}
