package io.syemessenger.api.message;

import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;

@Named
@RequestController
public class MessageController {

  private final MessageService messageService;

  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @RequestHandler(value = "v1/syemessenger/subscribe", requestType = Long.class)
  public void subscribe(SessionContext sessionContext, ServiceMessage serviceMessage) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var roomId = (Long) serviceMessage.data();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    messageService.subscribe(roomId, sessionContext.accountId(), sessionContext);

    sessionContext.send(serviceMessage.clone().data(roomId));
  }
}
