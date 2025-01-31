package io.syemessenger.api.account;

public interface AccountSdk {

  AccountInfo createAccount(CreateAccountRequest request);

  AccountInfo updateAccount(UpdateAccountRequest request);

  Long login(LoginAccountRequest request);

  AccountInfo getAccount(Long id);

  GetRoomsResponse getRooms(GetRoomsRequest request);
}
