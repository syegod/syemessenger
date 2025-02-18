package io.syemessenger.api.message;

public interface MessageSdk {

  void subscribe(Long roomId);

  void unsubscribe();

  void send(String message);
}
