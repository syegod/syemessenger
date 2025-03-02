package io.syemessenger.api.messagehistory;

import java.time.LocalDateTime;

public record MessageRecord(Long senderId, Long roomId, String message, LocalDateTime timestamp) {}
