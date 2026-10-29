package io.casehub.flow.service;

import java.util.List;

public record PlanView(List<List<PlanStep>> additions,
                        List<List<PlanStep>> removals,
                        List<List<PlanStep>> suspensions,
                        List<List<PlanStep>> resumptions,
                        boolean empty) {}
