package io.syemessenger.api.room.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

@Transactional
public interface BlockedRepository extends CrudRepository<BlockedMember, BlockedMemberId> {}
