package io.syemessenger;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    System.out.println("Hello world");
    Thread.currentThread().join();
  }
}
