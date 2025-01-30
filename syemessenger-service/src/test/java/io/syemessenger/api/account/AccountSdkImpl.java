package io.syemessenger.api.account;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceMessage;

public class AccountSdkImpl implements AccountSdk {

  private final ClientSdk clientSdk;

  public AccountSdkImpl(ClientSdk clientSdk) {
    this.clientSdk = clientSdk;
  }

  @Override
  public AccountInfo createAccount(CreateAccountRequest request) {
    final var message = new ServiceMessage().qualifier("createAccount").data(request);
    clientSdk.sendText(message);
    return (AccountInfo) clientSdk.pollResponse();
  }

  @Override
  public AccountInfo updateAccount(UpdateAccountRequest request) {
    final var message = new ServiceMessage().qualifier("updateAccount").data(request);
    clientSdk.sendText(message);
    return (AccountInfo) clientSdk.pollResponse();
  }

  @Override
  public Long login(LoginAccountRequest request) {
    final var message = new ServiceMessage().qualifier("login").data(request);
    clientSdk.sendText(message);
    return (Long) clientSdk.pollResponse();
  }

  @Override
  public AccountInfo getSessionAccount() {
    final var message = new ServiceMessage().qualifier("getSessionAccount");
    clientSdk.sendText(message);
    return (AccountInfo) clientSdk.pollResponse();
  }

  @Override
  public AccountViewInfo showAccount(Long id) {
    final var message = new ServiceMessage().qualifier("showAccount").data(id);
    clientSdk.sendText(message);
    return (AccountViewInfo) clientSdk.pollResponse();
  }

  @Override
  public GetRoomsResponse getRooms(GetRoomsRequest request) {
    final var message = new ServiceMessage().qualifier("getRooms").data(request);
    clientSdk.sendText(message);
    return (GetRoomsResponse) clientSdk.pollResponse();
  }
}
