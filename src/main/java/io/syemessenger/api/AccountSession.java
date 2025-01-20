package io.syemessenger.api;

import org.eclipse.jetty.websocket.api.Session;

public class AccountSession {

  private final Session session;
  private Long accountId;

  public AccountSession(Session session, Long accountId) {
    this.session = session;
    this.accountId = accountId;
  }

  public Session session() {
    return session;
  }

  public Long accountId() {
    return accountId;
  }

  public AccountSession accountId(Long accountId) {
    this.accountId = accountId;
    return this;
  }
}
