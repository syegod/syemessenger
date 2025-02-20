package io.syemessenger.api.message;

public interface MessageSdk {

  Long subscribe(Long roomId);

  Long unsubscribe();

  Long send(String message);
}
