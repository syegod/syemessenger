package io.syemessenger.api.room.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RoomRepository extends CrudRepository<Room, Long> {

  @NativeQuery(
      "SELECT * FROM rooms r JOIN room_members rm ON rm.room_id = r.id WHERE rm.account_id = ?1")
  Page<Room> findByAccountId(Long accountId, Pageable pageable);

  @Modifying
  @NativeQuery("INSERT INTO room_members VALUES (?1, ?2)")
  void saveRoomMember(Long roomId, Long accountId);

  Room findByName(String name);

  @Modifying
  @NativeQuery("DELETE FROM room_members rm WHERE rm.room_id = ?1 AND rm.account_id = ?2")
  void deleteRoomMember(Long roomId, Long accountId);

  @Modifying
  @NativeQuery("DELETE FROM room_members rm WHERE rm.room_id = ?1 AND rm.account_id IN ?2")
  void deleteRoomMembers(Long roomId, List<Long> memberIds);
}
