# Decisions — Ops Perspective (#52)

## D1: Pure-YAML project location

**Choice:** Git repo — a directory of YAML files versioned in git
**Alternatives:**
- Managed by ops perspective — ops is the authoring environment, stores internally
- Both (git primary, UI editing) — git source of truth with inline editing pushed back
**Rationale:** Git provides versioning, collaboration, and browsability for free. Pure-YAML projects are inherently browsable online. The ops perspective pulls from git to deploy.
**Trade-offs:** No inline editing from the ops UI (editing happens in IDE/git). Could add git-backed editing later.
**Sources:** HANDOFF.md design questions, user clarification
**Exploration:** quick
**Status:** captured

## D2: YAML project runtime

**Choice:** Scaffold is the generic runtime — a pure-YAML project is "scaffold + YAML files", no build step
**Alternatives:**
- Generated app — ops generates a minimal Quarkus project, builds, deploys JAR
- Interpreted mode — ops interprets YAML in its own process
**Rationale:** Scaffold already loads modules from classpath. For YAML projects, it loads definitions from a mounted directory instead. No per-project build step. This IS a perspective — scaffold configured as a YAML runner.
**Trade-offs:** YAML projects are constrained to what scaffold's module set supports. Custom Java logic requires switching to hybrid archetype.
**Sources:** User clarification — "this is part of the perspectives we discussed"
**Exploration:** quick
**Status:** captured

## D3: Deployment target diversity

**Choice:** Ops perspective manages any deployed app — scaffold-based (YAML) or custom Quarkus (Java/hybrid)
**Alternatives:**
- Always scaffold — force all apps through scaffold as plugin JARs
- Scaffold for YAML, custom for Java — split deployment paths
**Rationale:** Must deploy fsitrading and other hybrid Java/YAML apps as their own Quarkus processes. The ops perspective is an ops manager for a heterogeneous fleet, not a scaffold-only orchestrator.
**Trade-offs:** Two deployment paths to maintain (scaffold-based for YAML, custom Quarkus for Java/hybrid). More complexity in provisioning logic.
**Sources:** User clarification — "it has to be 3, so we can deploy fsitrading"
**Exploration:** quick
**Status:** captured

## D4: Deployment infrastructure

**Choice:** Podman for local dev mode, Kubernetes for production
**Alternatives:**
- K3s/minikube/Kind locally — full K8s API surface in dev, zero abstraction gap to production
- Podman only — K8s comes later
- Process-level — just run as local processes, no containers
**Rationale:** Podman provides lightweight, rootless container execution for local development without requiring a control plane or cluster setup. The ops perspective's DesiredState abstraction handles the differences between local Podman containers and production Kubernetes — each target gets its own ActualStateAdapter and Provisioner SPI implementations. K3s/minikube give closer K8s API parity but add control plane overhead; the DesiredState convergence engine is the parity mechanism, not the local runtime.
**Trade-offs:** Podman lacks K8s-specific primitives (service discovery, Ingress, RBAC, CRDs). The ops perspective's provisioning layer must abstract these differences through domain-specific DesiredState implementations. K3s would eliminate this abstraction gap at the cost of heavier local setup (~512MB baseline). Dev mode prioritises fast iteration over production fidelity.
**Sources:** HANDOFF.md design question #8, R1-02 review (corrected factual error — Podman does not emulate K8s)
**Exploration:** quick
**Status:** revised (R1-02: corrected rationale — Podman provides container isolation, not K8s emulation; DesiredState abstraction handles target differences)

## D5: Enhanced YAML experience — semantic operational views

**Choice:** For pure-YAML projects, render semantic views of designed state, manifests, and eidos orgs using blocks-ui diagram viewers. Not just raw YAML browsing — actual operational diagrams.
**Alternatives:**
- Visual modeller with drag-and-drop editing — higher effort, targets non-developers
- Read-only dashboard — runtime state only, no design-time visibility
**Rationale:** A pure-YAML project is fully declarative and inspectable. The ops perspective can understand the entire project structure and render: DesiredState topology, eidos org charts, LLM manifest configuration, channel wiring. For Java projects, these definitions are opaque (in code), so only runtime observability is available. This is the core differentiator of the pure-YAML enhanced experience.
**Trade-offs:** Requires blocks-ui components for each semantic view. Java projects get a less rich experience.
**Sources:** User clarification — "we want to show the designed state, the manifests, the eidos orgs"
**Exploration:** quick
**Status:** captured

## D6: Case-managed ops perspective

**Choice:** The ops perspective runs on the engine from day one — provisioning is a case, drift triggers situations, resolutions feed CBR
**Alternatives:**
- Layered in later — start imperative, add case management as phase 2
- Keep it imperative — no case modelling
**Rationale:** The ops perspective IS the CaseHub demo. Every ops concept the platform provides becomes both the product feature AND the live demonstration. Starting case-managed avoids a rewrite and proves the platform's full power.
**Trade-offs:** Adds complexity to the MVP. Case definitions for ops workflows must be designed alongside the ops perspective itself.
**Sources:** HANDOFF.md — "eating its own dog food"
**Exploration:** quick
**Status:** captured

## D7: Claudony integration

**Choice:** Deferred until worker pool model settles. Target integration surface: Claudony's session-list and terminal components for deployed-app agent observability. No dependency created until the worker pool model is defined.
**Alternatives:**
- Commit now — embed Claudony views in MVP despite unsettled worker pool model
- Link out to Claudony's own UI — looser coupling, no component embedding
- Defer entirely — not MVP, no target identified
**Rationale:** Claudony manages Claude Code sessions (fleet management, xterm.js terminals, persistent tmux sessions) and does not deploy apps — that's the ops perspective's domain. The Trellis precedent shows Claudony's primitives are reusable. But embedding views for agent observability requires the worker pool model to define what "agent session within a deployed app" means. Committing now would create a dependency on an undefined abstraction.
**Trade-offs:** Deferral means the ops perspective MVP has no agent session visibility for deployed apps. This is acceptable — agent observability is a Claudony concern, and link-out to Claudony's UI is a viable interim path.
**Sources:** Investigation — Claudony manages sessions not deployments; Trellis precedent; R1-07 review
**Exploration:** quick
**Status:** revised (R1-07: changed from aspirational to explicit deferral with identified integration primitives)

## D8: Cross-deployment CBR

**Choice:** Shared CBR case base across all managed deployments — resolutions in one deployment are available to all
**Alternatives:**
- Per-deployment CBR only — isolated, no fleet learning
- Defer to later phase
**Rationale:** This is where "CaseHub managing CaseHub" becomes genuinely powerful. The ops perspective learns from every deployment it manages. A situation resolved for fsitrading's agent scaling informs the next deployment with the same pattern.
**Trade-offs:** CBR retrieval must handle cross-deployment context (features from different domains may not be comparable). Needs careful feature engineering.
**Sources:** HANDOFF.md design question #4, engine CBR investigation (Retrieve/Reuse/Revise/Retain)
**Exploration:** quick
**Status:** captured

## D9: Fleet topology — shared scaffold for YAML apps

**Choice:** Mixed deployment topologies: dedicated process per app (Java/hybrid), shared scaffold instance hosting 1..n YAML apps, or dedicated scaffold per YAML app. Ops perspective manages all uniformly.
**Alternatives:**
- Dedicated only — every app gets its own process/container (simplest isolation, higher resource cost)
- Shared only — all YAML apps must co-locate (lowest cost, weakest isolation)
**Rationale:** An ops manager must manage 1..n apps. For pure-YAML apps, a shared scaffold instance is the "serverless" model — no per-app container overhead, scaffold handles scaling. But dedicated deployment must remain an option for isolation. Java apps are always dedicated. Pure-YAML apps are data (case definitions, DesiredState YAML, eidos definitions), not compiled code — they share the scaffold runtime and don't require classloader or CDI isolation.
**Trade-offs:** Shared scaffold hosting requires data-level isolation: DesiredState tenancy (each YAML app maps to a distinct `tenancyId`, giving it its own `ReconciliationLoop` instance, separate actual state tracking, and isolated fault handling via per-tenant `FaultCountStore`) keeps co-hosted YAML apps logically separated. Engine case definitions are scoped by namespace to prevent case type collisions. Resource-level isolation (thread pool partitioning, memory limits) is not yet supported — isolation is data-level only. More complex than dedicated-only, but the isolation mechanism is platform-native (DesiredState tenancy), not a custom classloader hack.
**Sources:** User clarification — "for pure YAML apps, you could be a container for 1..n deployments"; R1-04, R1-06, R2-03 reviews (corrected isolation mechanism and terminology)
**Exploration:** quick
**Status:** revised (R1-04: replaced classloader/CDI isolation claim with data-level isolation; R1-06: corrected terminology from "engine namespaces" to "DesiredState tenancy"; R2-03: removed false resource quota claim, acknowledged resource-level isolation is not yet supported)

## D10: Perspective definition format

**Choice:** Perspective as Module Composition + UI DSL (Approach B) — perspective YAML declares modules to activate AND includes an embedded UI DSL (casehub-pages DSL) defining custom views, datasets, and layout
**Alternatives:**
- Boot Configuration (Approach A) — YAML selects from existing modules/views only. Too limited — ops perspective needs custom views.
- Typed Extension Point (Approach C) — Java SPI with YAML adapter. Unnecessary split — if the pages DSL is expressive enough (current console is 100% DSL-driven), no Java SPI needed.
**Rationale:** The casehub-pages DSL is already proven — ~200 lines of DSL drives the entire current console with zero custom Lit components. A perspective YAML that includes module composition + pages DSL definitions is expressive enough for real perspectives while staying fully declarative.
**Trade-offs:** Bet on the pages DSL being sufficient. If a perspective needs UI behaviour the DSL can't express, the DSL must be extended rather than falling back to Java.
**Component resolution:** The current console uses TypeScript imports + `registerPanel()` to map panel names to web component tags. For YAML perspectives, scaffold bundles all available blocks-ui components in its webui build; the perspective YAML references them by registered panel name via the DSL's `hostPanel()` primitive. New components for the ops perspective are added to scaffold's webui build dependencies. No per-perspective dynamic import mechanism is needed — scaffold is the host runtime, and its build includes the component superset.
**Depends on:** D5 (semantic views require blocks-ui components that the DSL must be able to reference)
**Sources:** Platform investigation — scaffold console architecture, casehub-pages DSL; R1-09 review (surfaced component resolution gap)
**Exploration:** deep-analysis
**Status:** revised (R1-09: added component resolution mechanism — scaffold bundles components, perspective YAML references by panel name)

## D11: Introspection hierarchy — YAML-enhanced, Java baseline

**Choice:** Pure-YAML projects get semantic design-time views (topology, org charts, manifests) because their definitions are data-inspectable. Java/hybrid projects get runtime observability (health, metrics, scaling, CBR) as a baseline experience.
**Alternatives:**
- Uniform experience — same views for all project types, limited to what Java can support (runtime only)
- Java-first — design the richest experience for existing Java apps, add YAML enhancements later
**Rationale:** The distinction is structural, not preferential. YAML definitions are data that the ops perspective can parse, introspect, and render — DesiredState topology, eidos org charts, channel wiring, LLM manifest configuration. Java definitions are compiled code that cannot be introspected at the same level without runtime reflection. The runtime observability baseline (health, deployment status, scaling, CBR, drift detection) applies to ALL deployment types and is already more than what any CaseHub app has today. The YAML-enhanced experience is additive — it does not subtract from Java's baseline.
**Trade-offs:** Java app operators may perceive a feature gap. The baseline experience must be genuinely useful on its own, not a stub. The uniform-experience alternative would cap the ops perspective's value at the lowest common denominator, losing the declarative introspection advantage that makes pure-YAML projects compelling.
**Sources:** R1-10 review challenge — surfaced as implicit decision
**Exploration:** quick
**Status:** captured

## D12: Ops instance topology

**Choice:** Architecture-agnostic — the ops perspective's design supports both single-instance fleet management and environment-scoped instances (dev/staging/prod). Instance topology is a deployment decision, not an architectural constraint.
**Alternatives:**
- Single instance mandated — one ops perspective manages all environments
- Per-environment mandated — separate ops instances per environment boundary
**Rationale:** The engine's case workflow system supports different approval workflows, escalation paths, and automation policies per case type or namespace. Environment-specific behavior (auto-approve for dev, manual approval gates for prod) is expressed in case definitions and workflow configuration, not in instance topology. A single instance with namespace-scoped workflows and multiple instances with environment-scoped configuration are both valid deployment patterns.
**Trade-offs:** Not prescribing topology means the ops perspective must work correctly in both modes. Shared CBR across environments (D8) requires careful scoping if instances are separate — cross-environment CBR transfer needs the same infrastructure-level feature scoping as cross-deployment CBR.
**Sources:** R1-11 review challenge — surfaced as implicit decision
**Exploration:** quick
**Status:** captured
