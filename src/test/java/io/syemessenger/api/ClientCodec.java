package io.syemessenger.api;

import io.syemessenger.JsonMappers;
import io.syemessenger.MessageCodec;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountViewInfo;

public class ClientCodec extends MessageCodec {

  private static final ClientCodec INSTANCE = new ClientCodec();

  private ClientCodec() {
    super(
        JsonMappers.jsonMapper(),
        (map) -> {
          map.put("createAccount", AccountInfo.class);
          map.put("updateAccount", AccountInfo.class);
          map.put("login", Long.class);
          map.put("getSessionAccount", AccountInfo.class);
          map.put("showAccount", AccountViewInfo.class);
          map.put("error", ErrorData.class);
        });
  }

  public static ClientCodec getInstance() {
    return INSTANCE;
  }
}
