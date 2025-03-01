package io.syemessenger.api.message.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MessageRepository extends CrudRepository<Message, Long> {}
