package io.syemessenger.outboxprocessor.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RoomEventRepository extends CrudRepository<OutboxRoomEvent, Long> {

  @NativeQuery("SELECT * FROM outbox_room_events ORDER BY id ASC OFFSET :position LIMIT 500")
  List<OutboxRoomEvent> findOutboxRoomEvents(@Param("position") long position);

  @NativeQuery(
      "INSERT INTO outbox_position VALUES (1, :position) "
          + "ON CONFLICT (id) DO UPDATE SET position = :position")
  void saveRoomEventPosition(@Param("position") long position);

  @NativeQuery("SELECT position FROM outbox_position WHERE id = 1")
  Long getPosition();
}
