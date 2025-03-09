package io.syemessenger.api.messagehistory;

import java.time.LocalDateTime;

public record MessageRecord(
    Long id, Long senderId, Long roomId, String message, LocalDateTime timestamp) {}
