package io.syemessenger.api.messagehistory.repository;

import java.sql.Timestamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HistoryMessageRepository extends CrudRepository<HistoryMessage, Long> {

  @NativeQuery("SELECT * FROM messages")
  Page<HistoryMessage> findAll(Pageable pageable);

  @NativeQuery(
      "SELECT * FROM messages m "
          + "WHERE lower(m.message) LIKE '%' || lower(:keyword) || '%' "
          + "AND m.timestamp BETWEEN :from AND :to ")
  Page<HistoryMessage> findByKeywordAndTimestamp(
      @Param("keyword") String keyword,
      @Param("from") Timestamp from,
      @Param("to") Timestamp to,
      Pageable pageable);
}
