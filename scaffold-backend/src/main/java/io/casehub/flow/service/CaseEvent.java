package io.casehub.flow.service;

import java.time.Instant;

public record CaseEvent(String eventType, Instant timestamp, String actorId, String detail) {}
