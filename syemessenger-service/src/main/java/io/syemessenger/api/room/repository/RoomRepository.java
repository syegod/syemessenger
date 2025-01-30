package io.syemessenger.api.room.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RoomRepository extends CrudRepository<Room, Long> {}
