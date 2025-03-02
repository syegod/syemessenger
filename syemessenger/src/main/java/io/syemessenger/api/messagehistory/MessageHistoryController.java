package io.syemessenger.api.messagehistory;

import io.syemessenger.annotations.RequestController;
import io.syemessenger.annotations.RequestHandler;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.websocket.SessionContext;
import jakarta.inject.Named;

@Named
@RequestController
public class MessageHistoryController {

  private final MessageHistoryService messageHistoryService;

  public MessageHistoryController(MessageHistoryService messageHistoryService) {
    this.messageHistoryService = messageHistoryService;
  }

  @RequestHandler(value = "v1/syemessenger/listMessages", requestType = ListMessagesRequest.class)
  public void listMessages(SessionContext sessionContext, ServiceMessage message) {
    if (!sessionContext.isLoggedIn()) {
      throw new ServiceException(401, "Not authenticated");
    }

    final var request = (ListMessagesRequest) message.data();

    final var roomId = request.roomId();
    if (roomId == null) {
      throw new ServiceException(400, "Missing or invalid: roomId");
    }

    final var offset = request.offset();
    if (offset != null && offset < 0) {
      throw new ServiceException(400, "Missing or invalid: offset");
    }

    final var limit = request.limit();
    if (limit != null && (limit < 0 || limit > 50)) {
      throw new ServiceException(400, "Missing or invalid: limit");
    }

    // TODO: implement
    final var from = request.from();
    final var to = request.to();
    final var timezone = request.timezone();

    if (from != null || to != null) {
      if (timezone == null) {
        throw new ServiceException(400, "Missing or invalid: timezone");
      }
    }

    final var keyword = request.keyword();
    if (keyword != null && (keyword.length() < 3 || keyword.length() > 64)) {
      throw new ServiceException(400, "Missing or invalid: keyword");
    }

    final var messagePage = messageHistoryService.listMessages(sessionContext, request);

    final var messageInfos =
        messagePage.getContent().stream().map(MessageMappers::toMessageInfo).toList();

    final var response =
        new ListMessagesResponse()
            .messages(messageInfos)
            .limit(limit)
            .offset(offset)
            .totalCount(messagePage.getTotalElements());

    sessionContext.send(message.clone().data(response));
  }
}
