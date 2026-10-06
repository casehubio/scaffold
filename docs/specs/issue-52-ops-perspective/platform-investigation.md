# Platform Investigation — Ops Perspective (#52)

## Platform Architecture (4 Tiers)

| Tier | Repos | Role |
|------|-------|------|
| **Foundation** | platform, ledger, work, qhorus, eidos, desiredstate, connectors, iot, ras, neocortex, worker | Domain-agnostic embeddable modules. Hexagonal — pure-Java SPI `api/` modules. |
| **Orchestration** | casehub-engine | Hybrid choreography+blackboard. Depends on foundation only. |
| **Integration** | claudony, casehub-ops, scaffold | Wire things together. |
| **Application** | devtown, aml, clinical, life, fsitrading, quarkmind, soc, drafthouse | Domain-specific apps. |

**Dependency rule:** source code deps point inward only. Domain logic never depends on infrastructure.

## Three Project Archetypes

### 1. Pure YAML

Two declaration surfaces, no Java beyond a Quarkus app runner:

- **Engine YAML DSL** — case definitions (capabilities, milestones, goals, bindings, workers, CBR config). Uses yaml-core modules/variables/forEach.
- **DesiredState Plugin YAML** — full provisioner lifecycle in YAML: spec schema, actual-state checks (via step primitives: rest-call, compare-state, assert), provisioner steps, fault policy, CBR features, RAS situations.

**Status:** Plugin model exists only in test fixtures. No real consumer project uses pure-YAML plugins. Viable for REST-based resource domains.

**Limitations (requires Java):**
- Custom ActualStateAdapter logic beyond step-based model
- Custom provisioner logic beyond REST calls and assertions
- Custom fault policies beyond threshold-based escalation
- Custom SituationRecompiler implementations
- Custom workers/agents (backends are Java)
- Complex expression evaluation beyond JQ/MVEL
- Custom CBR retrieval/adaptation strategies

### 2. Java

Programmatic GoalCompiler, Java NodeSpecs, Java provisioners. Full control. All current real apps (fsitrading, aml, clinical, life).

### 3. Hybrid

YAML graph declarations + YAML case definitions + Java for complex domain logic, custom workers, custom SPIs. fsitrading is the best example: Java domain logic (SPIs, strategies) + YAML case definitions + YAML deployment topology.

## Key Platform Capabilities

### yaml-core (platform/)
Primitive layer — zero-dependency, J2CL-transpilable. Provides: VariableResolver, ForEachExpander, Module system (parameters, constraints, aliasing, cross-module type checking), CsvParser, Truthiness. Does NOT contain domain constructs.

### DesiredState
Five declaration surfaces: Java API, annotations, YAML, TypeScript DSL, plugin YAML.

**Convergence engine:** GoalCompiler → ActualStateAdapter → TransitionPlanner → TransitionExecutor → ReconciliationLoop. CBR chain for fault escalation (per-node) and situation recompilation (whole-graph). Cross-domain composition via CrossDomainCompositionEngine.

### Ops (casehub-ops)
Six domain modules:

| Module | Domain | Status |
|--------|--------|--------|
| deployment/ | Agent topology (agents, channels, case types, trust, endpoints) | PRIMARY — active development |
| infra/ | Infrastructure (K8s, Terraform, Ansible) | PoC |
| compliance/ | Regulatory posture (SOC2, GDPR, EU-AI-Act, DORA, NIS2) | GRC play |
| iot/ | IoT device desired state | Secondary |
| app/ | Ops console (K8s lifecycle management) | Console |
| testing/ | Test fixtures | Test only |

Deployment module: adaptive topology (RAS-driven scaling, trust updates, node additions), 5 drift checkers, 5 provisioners, all Java.

### Engine
Case lifecycle (PlanItem states: PENDING→ENABLED→ACTIVE→RUNNING→COMPLETED/TERMINATED/FAILED/OBSOLETE). Hybrid choreography+blackboard. CBR: Retrieve/Reuse/Revise/Retain across neocortex, ledger, engine. REST/GraphQL/MCP surface.

### Eidos
4-layer agent identity (identity, slot, capabilities, disposition). VocabularyRegistry, AgentRegistry, CapabilityHealth, org model (supervision/delegation/escalation), YAML-driven registration, SystemPromptRenderer.

### Qhorus
Embedded library — not a standalone service. Speech-act typed messages (10 types), typed channels (5 semantics), shared data store, instance registry, normative audit ledger, A2A protocol. Each app has its own Qhorus + PostgreSQL. Multi-node via postgres-broadcaster (LISTEN/NOTIFY).

### Claudony
Manages Claude Code sessions — fleet management, xterm.js terminals, persistent tmux sessions, case browser, action inbox. CaseHub SPI implementations (worker provisioner, case channels, context, status). Reusable primitives — Trellis already consumes them. **Does not deploy apps** — that's the ops perspective's domain.

### LLM Manifests (platform agent-config-core/)
agent-config.yaml — declares providers (vendor + credential), aliases (tier/capability/context constraints), defaults. Framework-neutral, directory hierarchy discovery, recursive remote source loading. **Dated 2026-09-16 — zero adoption in any example or consumer.**

## Scaffold Architecture

2 Maven modules (parent + scaffold-backend). Quinoa web console. 10 views (~200 lines TS total). Module detection via classpath probing (`ModuleRegistryResource`). Conditional tabs for ops/iot. No perspective concept. No desiredstate or ops dependencies.

## Gaps

1. **No pure-YAML consumer project exists** — plugin model only in test fixtures
2. **LLM manifests not adopted** — agent-config.yaml is 5 days old, no consumers
3. **Ops deployment module is Java-only** — no plugin YAML for deployment node types
4. **No perspective concept** in platform or scaffold
5. **Examples not updated** — fsitrading doesn't use manifests or plugin model
6. **Cross-domain composition** only consumed by ops app module

## Where Perspectives Fit

Integration tier. A perspective is a **configured composition** — it declares which foundation/orchestration modules to include, which UI views to render, and what workflows it optimises for. The module detection pattern (classpath probing → conditional tabs) is the seed.
