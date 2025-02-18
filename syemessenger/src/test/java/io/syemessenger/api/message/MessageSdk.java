package io.syemessenger.api.message;

import io.syemessenger.api.Flux;

public interface MessageSdk {

  Flux<String> subscribe(Long roomId);

  void unsubscribe();

  void send(String message);
}
