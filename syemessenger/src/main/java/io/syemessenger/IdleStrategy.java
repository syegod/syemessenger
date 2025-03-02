package io.syemessenger;

import jakarta.inject.Named;

public interface IdleStrategy {

  void idle(long value);
}
