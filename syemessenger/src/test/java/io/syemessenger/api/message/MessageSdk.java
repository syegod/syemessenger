package io.syemessenger.api.message;


import io.syemessenger.api.Receiver;

public interface MessageSdk {

  Receiver subscribe(Long roomId);

  void unsubscribe();

  void send(String message);
}
