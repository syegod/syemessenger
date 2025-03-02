package io.syemessenger.api.messagehistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HistoryMessageRepository extends CrudRepository<HistoryMessage, Long> {

  @NativeQuery("SELECT * FROM messages")
  Page<HistoryMessage> findAll(Pageable pageable);

  Page<HistoryMessage> findByMessageContaining(String keyword, Pageable pageable);
}
