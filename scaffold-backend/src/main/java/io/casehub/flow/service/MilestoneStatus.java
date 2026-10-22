package io.casehub.flow.service;

import java.time.Instant;

public record MilestoneStatus(String name, boolean completed, Instant deadline, boolean breached) {}
