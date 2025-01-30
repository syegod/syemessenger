package io.syemessenger.api.account;

public interface AccountSdk {

  AccountInfo createAccount(CreateAccountRequest request);

  AccountInfo updateAccount(UpdateAccountRequest request);

  Long login(LoginAccountRequest request);

  AccountInfo getSessionAccount();

  AccountViewInfo showAccount(Long id);

  RoomList getRooms(GetRoomsRequest request);
}
