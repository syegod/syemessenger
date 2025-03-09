package io.syemessenger.environment;

import java.time.LocalDateTime;

public record FromToTimestamp(LocalDateTime from, LocalDateTime to, String timezone) {}
