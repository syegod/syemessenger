package io.syemessenger.api.account.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AccountRepository extends CrudRepository<Account, Long> {

  Account findByEmailOrUsername(String email, String username);
}
