package io.syemessenger;

public class SleepIdleStrategy implements IdleStrategy {

  @Override
  public void idle(long value) {
    try {
      Thread.sleep(value);
    } catch (InterruptedException e) {
      // TODO Oleh: maybe to addition of rethrowing we need to do more with InterruptedException?
      throw new RuntimeException(e);
    }
  }
}
