package io.syemessenger.api.room.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RoomRepository extends CrudRepository<Room, Long> {

  @NativeQuery(
      "SELECT * FROM rooms r JOIN room_members rm ON rm.room_id = r.id WHERE rm.account_id = ?1")
  Page<Room> findByAccountId(Long accountId, Pageable pageable);
}
