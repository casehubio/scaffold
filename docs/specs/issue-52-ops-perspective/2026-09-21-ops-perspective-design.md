# Ops Perspective Design — scaffold #52

## Overview

The ops perspective is a configured scaffold deployment that provisions, deploys, and manages CaseHub applications using the platform's own DesiredState and Ops capabilities. It manages a heterogeneous fleet of deployments — pure-YAML projects running on scaffold, Java apps, and hybrid Java/YAML apps — with uniform health monitoring, drift detection, situation resolution, and cross-deployment CBR learning.

The ops perspective is itself a CaseHub application: its operations are modelled as cases running on the engine. Every ops concept the platform provides becomes both the product feature AND the live demonstration.

## 1. Architecture — The Perspective Concept

A **perspective** is a YAML-defined configuration that tells scaffold what to be. It declares which platform modules to activate, which UI views to render (using the casehub-pages DSL), and what datasets to wire in.

```yaml
perspective:
  name: ops
  description: "CaseHub operations management"
  version: "1.0"
  
  modules:
    required: [engine, desiredstate, ops-deployment, ops-infra, work, qhorus, eidos]
    optional: [ops-compliance, ops-iot]
  
  views:
    tabs:
      - id: fleet
        label: "Fleet"
        view: fleet-dashboard
      - id: deploy
        label: "Deploy"
        view: deployment-wizard
      # ... (full tab definitions in Section 6)
  
  datasets:
    deployments:
      endpoint: /api/ops/deployments
      source: ops-app
    health:
      endpoint: /api/ops/health
      source: ops-app
    drift:
      endpoint: /api/ops/drift
      source: desiredstate
```

### Perspective loading mechanism

**Schema:** The `app.yaml` schema above is the complete top-level structure. Fields: `name` (string, required), `description` (string), `version` (semver string), `modules` (required/optional lists of module identifiers), `views` (tabs array, each with `id`, `label`, `view` referencing a pages DSL view definition), `datasets` (named data sources with `endpoint` and `source` identifying the providing module).

**Module activation:** Today, `ModuleRegistryResource` does classpath probing (`probeClass("io.casehub.ops.app.OpsApplication", "ops", detected)`) to discover available modules. Perspectives replace probing with declaration: the perspective YAML lists required and optional modules. At boot, scaffold validates that all required module classes are on the classpath and activates CDI beans via `@DesiredStateQualifier`-style scoping. Optional modules activate only when their probe class is present. This is a build-time composition decision — the scaffold Maven POM declares which module JARs to include, and the perspective YAML declares which of those to activate.

**View rendering:** Scaffold reads `app.yaml` at boot. The views section defines the tab layout using the casehub-pages DSL. Each `view` reference maps to a pages DSL definition (either inline in the perspective YAML or in a separate view file). The pages DSL's `hostPanel()` primitive references blocks-ui components by registered panel name. All blocks-ui components are bundled in scaffold's webui build; the perspective references them by name.

**Dataset wiring:** Each dataset entry declares an endpoint path and the source module that provides it. The ops-app module already exposes REST resources (`ApplicationResource`, `DeploymentResource`, `ReconciliationResource`, `ClusterResource`, `ScalingResource`, `CaseResource`, `ApprovalResource`, `ServiceOperationResource`). The perspective's dataset declarations wire these existing endpoints to frontend data consumers via the pages DSL's data binding mechanism.

### Three runtime modes

| Mode | What it is | Example |
|------|-----------|---------|
| **Ops perspective** | Scaffold configured to manage a fleet of CaseHub deployments | The ops perspective itself |
| **YAML runner** | Scaffold configured to run a pure-YAML project (case definitions, DesiredState graphs, eidos orgs — all loaded from YAML) | A pure-YAML CaseHub app |
| **Generic console** | Today's scaffold — classpath detection, conditional tabs | The current #41/#48 console |

The generic console is the "default perspective" — what scaffold does today when no `app.yaml` is present.

### Component resolution

The current console uses TypeScript imports + `registerPanel()` to map panel names to web component tags. For YAML-defined perspectives, scaffold bundles all available blocks-ui components in its webui build; the perspective YAML references them by registered panel name. New components for the ops perspective are added to scaffold's webui build dependencies. No per-perspective dynamic import mechanism is needed — scaffold is the host runtime, and its build includes the component superset.

This changes the build model: today, each view imports only what it needs. With perspectives, scaffold's webui build includes all blocks-ui packages as dependencies. Lit web components register via `customElements.define()` — a side effect that tree-shakers cannot eliminate — so the production bundle includes all registered components regardless of which perspective uses them. The bundle size increase is the cost of the component superset approach; it is acceptable because scaffold is a local dev tool, not a public-facing web app where bundle size is critical. This is a real build change — scaffold's `package.json` gains dependencies on all blocks-ui component packages, and the Quinoa build step includes them in the webui output.

## 2. Deployment Model — Three Archetypes, Uniform Management

The ops perspective manages a heterogeneous fleet. How an app gets deployed depends on its archetype; how it gets managed is uniform.

### Provisioning flows by archetype

| Archetype | Input | Build step | Runtime | Deployment artifact |
|-----------|-------|-----------|---------|-------------------|
| **Pure YAML** | Git repo of YAML files | None | Scaffold instance loads YAML from mounted volume | Container running scaffold + volume-mounted YAML |
| **Java** | Maven project (GAV coordinates) | `mvn package` → uber-JAR | Own Quarkus process | Container running the app JAR |
| **Hybrid** | Maven project + YAML definitions | `mvn package` (YAML included in JAR resources) | Own Quarkus process | Container running the app JAR |

### Fleet topologies

For pure-YAML apps, two deployment options:

- **Dedicated** — one scaffold container per YAML app. Full isolation.
- **Shared** — multiple YAML apps co-hosted in a single scaffold instance. Data-level isolation via DesiredState tenancy: each YAML app maps to a distinct `tenancyId`, giving it its own `ReconciliationLoop` instance, separate actual state tracking, and isolated fault handling via per-tenant `FaultCountStore`. Engine case definitions are scoped by namespace (e.g. `.namespace("app-a")`, `.namespace("app-b")`) to prevent case type collisions between co-hosted apps. Resource-level isolation (thread pool partitioning, memory limits) is not yet supported and would require new runtime capabilities. Lower overhead, "serverless" feel.

**YAML-app-to-tenancyId mapping:** Each YAML app in a shared scaffold gets a tenancyId derived from its project identifier (e.g. `yaml-app:{project-name}`). This maps to a dedicated `ReconciliationLoop` instance via `LifecycleManager.start(tenancyId, graph)` and scopes all DesiredState operations — `ProvisionContext.tenancyId()`, `DeprovisionContext.tenancyId()`, CloudEvent correlation keys, and `TenancyFilter` for REST request scoping — to that app.

Java/hybrid apps are always dedicated — they're their own Quarkus processes.

The ops perspective manages all of these uniformly through DesiredState. Two distinct DesiredState layers operate for each managed deployment:

**Infrastructure DesiredState** (ops perspective's concern):
- Managed by the `ops-container` module (`ContainerGoalCompiler`, `PodmanActualStateAdapter`)
- Graph nodes: containers, databases, networks, volumes (`ContainerNodeSpec` hierarchy)
- `ReconciliationLoop` runs in the ops perspective process
- Drift means: container stopped, wrong image version, port mapping changed, volume missing

**Application DesiredState** (deployed app's concern):
- Managed by the `ops-deployment` module (`DeploymentGoalCompiler`) within the deployed app
- Graph nodes: agents, channels, case types, trust policies, endpoints, detections (`DeploymentNodeSpec` hierarchy — `AgentNodeSpec`, `ChannelNodeSpec`, `CaseTypeNodeSpec`, `TrustPolicyNodeSpec`, `EndpointNodeSpec`, `DetectionNodeSpec`)
- `ReconciliationLoop` runs in the deployed app process (or in the shared scaffold instance for co-hosted YAML apps)
- Drift means: agent not registered in eidos, channel misconfigured, case type missing

The ops perspective owns and operates the infrastructure layer. It observes the application layer — either in-process (shared scaffold) or via remote query (dedicated/Java deployments). The convergence model is the same at both layers; the provisioning and actual-state-checking implementations differ per deployment target (Podman containers locally, K8s pods in production).

### Infrastructure per deployment

Each deployed CaseHub app needs supporting infrastructure:

- **Database** — PostgreSQL (for engine, qhorus, ledger)
- **Qhorus** — embedded in the app (library dependency), but needs its own DB schema
- **LLM backing** — configured via agent-config manifest (provider, aliases, defaults)

The ops perspective provisions this infrastructure alongside the app. For Podman dev mode, this means spinning up a PostgreSQL container and configuring the app container to connect to it.

### Deployment targets

- **Local dev mode** — Podman containers on the developer's machine. Rootless, lightweight, no cluster required.
- **Production** — Kubernetes. Same DesiredState model, different ActualStateAdapter and Provisioner SPI implementations.

The DesiredState abstraction handles the differences between targets. Each target implements the same five SPIs (GoalCompiler, ActualStateAdapter, NodeProvisioner, FaultPolicy, EventSource) from `casehub-desiredstate-api`, following the pattern established by ops domain modules (infra, deployment, compliance, IoT).

### Podman provisioning layer

The Podman provisioning layer is a new ops domain module (`ops-container`) implementing the DesiredState SPI quad for container management.

**DesiredState node types:**

```
ContainerNodeSpec (sealed interface extends NodeSpec)
├── AppContainerSpec      — CaseHub app container (image, ports, env, volume mounts, resource limits)
├── DatabaseContainerSpec — PostgreSQL container (version, database name, credentials, data volume)
├── NetworkSpec           — Podman network for inter-container connectivity
└── VolumeSpec            — Named volume for persistent data (YAML projects, database data)
```

**SPI implementations:**

| SPI | Implementation | Responsibility |
|-----|---------------|----------------|
| GoalCompiler | `ContainerGoalCompiler` | Deployment descriptor → DesiredStateGraph with container, database, network, and volume nodes. Database depends on network; app container depends on database + network. |
| ActualStateAdapter | `PodmanActualStateAdapter` | Queries Podman socket API (`/v5.0.0/libpod/containers/json`, `/volumes/json`, `/networks/json`) for container/volume/network state. Maps Podman status to `NodeStatus` (running → PRESENT, exited → DRIFTED, absent → ABSENT). |
| NodeProvisioner | `PodmanNodeProvisioner` | Sealed type dispatch to handlers. `podman create`, `podman start`, `podman network create`, `podman volume create` via Podman REST API. |
| FaultPolicy | `ContainerFaultPolicy` | After 3 consecutive PROVISION_FAILED events, adds a `container-review` human node for investigation. |
| EventSource | `PodmanEventSource` | Subscribes to Podman event stream (`/events?filters=...`) for container state changes; emits `StateEvent` on stop/die/start events. Periodic resync at 30s for silent drift. |

**Podman API surface:** The Podman REST API (socket at `$XDG_RUNTIME_DIR/podman/podman.sock`) provides all operations needed: container lifecycle, network management, volume management, image pulls, and event streaming. No CLI wrapping needed — the REST API is the stable interface.

**K8s production path:** The existing ops/app module already implements K8s lifecycle management with `KubernetesActualStateAdapter`, `KubernetesNodeProvisioner`, `K8sDeploymentHandler`, `K8sServiceHandler`, `K8sIngressHandler`, `K8sConfigMapHandler`, `K8sNamespaceHandler`, `K8sWatchManager`, and `K8sClientRegistry`. The production path reuses this infrastructure.

## 3. Case-Managed Operations

The ops perspective is itself a CaseHub application. Its operations are modelled as cases running on the engine.

### Relationship to ops/app module

The ops `app/` module already implements case descriptors for operational workflows:

| Existing case descriptor | Maps to ops perspective concern |
|---|---|
| `DriftRemediationCaseDescriptor` | Drift classification, severity assessment, node-level remediation |
| `IncidentResponseCaseDescriptor` | Incident detection, escalation, resolution |
| `ScalingEventCaseDescriptor` | RAS-driven scaling |
| `ServiceUpgradeCaseDescriptor` | Canary/rolling upgrade workflow |
| `ComplianceRemediationCaseDescriptor` | Regulatory posture remediation |
| `CveResponseCaseDescriptor` | CVE triage and patching |
| `ApplicationCaseDescriptor` | Application lifecycle case |

The ops perspective uses these existing Java case descriptors as its operational backbone. It does not redefine them in YAML. The K8s lifecycle infrastructure in ops/app (`KubernetesActualStateAdapter`, `KubernetesNodeProvisioner`, `K8sWatchManager`, `K8sClientRegistry`, etc.) becomes the production deployment target adapter.

### Service lifecycle model (ops C5)

The ops/app module's `ServiceCaseDescriptor` implements the Service Lifecycle Domain Model (ops Chapter 5, in progress) — a nine-dimension operational model for long-lived service cases:

| Dimension | Status type | Example bindings |
|---|---|---|
| Health monitoring | `HealthStatus` | `health:incident-response`, `health:auto-restart` |
| Configuration drift | `DriftStatus` | `drift:auto-reconciliation`, `drift:manual-review` |
| Compliance | `ComplianceStatus` | `compliance:evidence-recollection`, `compliance:remediation` |
| Scaling | `ScalingStatus` | `scaling:scale-out` |
| Change management | `ChangeStatus` | `change:rolling-upgrade` |
| Security | `SecurityStatus` | `security:vulnerability-patch`, `security:breach-investigation` |
| Maintenance | `MaintenanceStatus` | `maintenance:scheduled` |
| Problem management | `ProblemStatus` | `problems:root-cause-investigation` |
| Decommission | `DecommissionStatus` | `decommission:data-migration`, `decommission:resource-cleanup` |

The ops perspective's fleet management maps directly onto this model. Each managed deployment IS a long-lived service case with these nine dimensions. The spec's 5 core case types (provisioning, drift, scaling, update, incident) are a subset of the 9 service lifecycle dimensions — they correspond to the initial operational concerns for fleet management. The remaining dimensions (compliance, security, maintenance, problem management, decommission) are available through the existing case descriptors.

### Core operational concerns

| Concern | Backing case descriptor | Trigger |
|---|---|---|
| **Provisioning** | `ApplicationCaseDescriptor` | User requests deployment of an app |
| **Drift resolution** | `DriftRemediationCaseDescriptor` | DesiredState convergence detects drift |
| **Scaling** | `ScalingEventCaseDescriptor` | RAS situation (load threshold, degraded health) |
| **Update** | `ServiceUpgradeCaseDescriptor` | New version available or manifest change |
| **Incident** | `IncidentResponseCaseDescriptor` | Health check failure, crash, unrecoverable fault |

### Cross-deployment CBR

The ops perspective maintains a shared CBR case base across all managed deployments using two complementary mechanisms:

**DesiredState configuration CBR** — `CbrFaultPolicy` and `CbrSituationRecompiler` in the DesiredState runtime handle infrastructure-level learning. When a fault occurs during convergence, `CbrFaultPolicy` retrieves similar past graph configurations via `ConfigurationRetriever`, adapts them to the current context via `ConfigurationAdapter`, and applies graph mutations. `CbrSituationRecompiler` does the same at the whole-graph level when RAS situations trigger replanning. These operate on DesiredState graph similarity — "a container topology that recovered from this failure pattern before."

**Engine case-level CBR** — `RetrievedExperience` in the engine provides case resolution learning. Past cases with similar problems are retrieved with their solutions, outcomes, plan traces, and confidence scores. This operates on operational decision similarity — "an incident response strategy that worked for this type of failure before."

For cross-deployment learning, both mechanisms scope retrieval by typed features:
- Deployment archetype (pure-YAML, Java, hybrid)
- Infrastructure type (Podman, K8s)
- Situation category (drift, scaling, fault)
- Scale (agent count, resource tier)

A scaling resolution for a 3-agent YAML app is relevant to a 5-agent YAML app but less relevant to a 50-agent Java trading platform. Feature similarity scoring handles this scoping.

### Instance topology

The ops perspective's architecture supports both single-instance fleet management and environment-scoped instances (dev/staging/prod). Instance topology is a deployment decision, not an architectural constraint. Environment-specific behaviour (auto-approve for dev, manual approval gates for prod) is expressed in case definitions and workflow configuration, not in instance topology.

## 4. Enhanced YAML Experience — Semantic Operational Views

The core differentiator for pure-YAML projects. Because every definition is data, the ops perspective can render rich semantic views.

### View hierarchy by project archetype

| View | Pure YAML | Java/Hybrid |
|------|-----------|-------------|
| **App topology** | Full application DesiredState graph (agents, channels, case types) — nodes, dependencies, convergence status | Runtime convergence status only |
| **Infrastructure topology** | Container DesiredState graph (containers, databases, networks, volumes) | Container DesiredState graph |
| **Eidos org chart** | Agent identities, capabilities, supervision/delegation/escalation relationships | Runtime agent registry only |
| **LLM manifest** | Provider config, aliases, tier assignments, credential status | Runtime model availability only |
| **Channel wiring** | Full qhorus topology — channels, semantics, participant assignments | Runtime channel state |
| **Case definitions** | Capability structure, milestones, goals, worker bindings | Runtime case instances and plan items |
| **Health dashboard** | Yes | Yes |
| **Drift detection** | Yes — with design-vs-actual overlay | Yes — actual state only |
| **CBR history** | Yes | Yes |
| **Fleet overview** | Yes | Yes |

For pure-YAML projects, every view has a **design-time + runtime** layer. You see what was intended AND what's actually happening. For Java projects, the runtime observability baseline (health, deployment status, scaling, CBR, drift detection) is genuinely useful on its own — more than what any CaseHub app has today. The YAML-enhanced experience is additive.

### Data source classification

The ops perspective consumes data from three distinct sources. Each has a different access pattern:

| Source | What it provides | Access pattern | Example |
|---|---|---|---|
| **Local (ops perspective state)** | Fleet registry, container topology, provisioning cases, deployment records, fleet-level health | CDI service injection — runs in the ops perspective process | `ApplicationResource`, `DeploymentResource`, `ClusterResource` |
| **Design-time (YAML parsing)** | Agent definitions, case definitions, channel wiring, manifests — for pure-YAML apps whose YAML files are available to the ops perspective | File parsing — no network call, data comes from the git-pulled YAML files | Parsing `casehub-deployment.yaml` to show agent topology |
| **Runtime (deployed app state)** | Agent health, active case instances, channel message flow, convergence status — data inside the deployed app | Remote REST call via `DeploymentDataProxy` (see below) | Querying a deployed app's eidos agent registry |

**Deployed app data access — `DeploymentDataProxy`:**

For dedicated scaffold instances and Java/hybrid apps, runtime data lives inside the deployed app (a separate process in a separate container). The ops perspective queries this data via a proxy service:

1. **URL resolution:** The ops perspective manages the container lifecycle. `PodmanActualStateAdapter` queries the Podman API for container state, including IP address and port mappings. For K8s, `KubernetesActualStateAdapter` resolves the Service endpoint. The resolved endpoint URL is stored on the `AppContainerSpec` node's actual state. The `DeploymentDataProxy` resolves the target URL from the DesiredState graph's actual state for the deployment's app container node.

2. **Request forwarding:** Sub-tab views call the proxy with a deployment ID and resource path. The proxy resolves the deployment's container endpoint and forwards: `GET /api/deployments/{id}/proxy/eidos/agents` → `GET http://{container-ip}:{port}/api/eidos/agents`.

3. **Authentication:** Dev mode (Podman) — no auth needed; containers share a Podman network and communicate over internal IPs. Production (K8s) — service account tokens or a shared secret configured per deployment in `ClusterReferenceEntity.credentialRef`.

4. **Failure handling:** If the deployed app is unreachable, the proxy returns HTTP 503 with a structured error. The sub-tab renders an "app unreachable" indicator. No stale data caching — the sub-tab shows live state or an explicit error.

**Shared scaffold optimisation:** For co-hosted YAML apps, the ops perspective IS the scaffold process running the apps. Runtime data is accessible in-process via CDI service injection scoped by `tenancyId` — no network call, no proxy. The `DeploymentDataProxy` detects shared tenants and routes to local CDI services instead of remote REST.

### blocks-ui components needed

The semantic views render via blocks-ui diagram viewers. Each component implements the pages `ConfigurablePanel` contract — data flows from REST endpoints through the pages DSL data binding to the component's `DataReceiver` interface.

| Component | Data source type | Endpoint (ops perspective) | JSON shape |
|---|---|---|---|
| **Graph viewer** | Runtime (proxied) or Design-time | `/api/deployments/{id}/topology` | `{nodes: [{id, type, status, spec}], edges: [{from, to}]}` |
| **Org chart viewer** | Runtime (proxied) or Design-time | `/api/deployments/{id}/agents` | `{agents: [{id, name, capabilities, supervisor, delegates}]}` |
| **Manifest table** | Design-time (YAML parsed) | `/api/deployments/{id}/manifests` | `{providers: [{name, vendor}], aliases: [{name, provider, tier}], defaults: {alias}}` |
| **Channel topology** | Runtime (proxied) or Design-time | `/api/deployments/{id}/channels` | `{channels: [{id, name, semantic, participants, rateLimit}]}` |
| **Case structure** | Runtime (proxied) or Design-time | `/api/deployments/{id}/cases` | `{definitions: [{name, namespace, capabilities: [{name, milestones, goals}]}]}` |

All endpoints are scoped under `/api/deployments/{id}/...` — the ops perspective resolves data source type (local design-time parse, remote proxy, or in-process shared tenant) based on the deployment's archetype and topology. For pure-YAML apps, design-time data from YAML parsing provides the structural view; runtime data from the deployed app provides convergence status and health. For Java/hybrid apps, only runtime data is available.

Component-level design specs will define exact `DataReceiver` interfaces and rendering behaviour during implementation.

These are wired into the ops perspective's view definitions via the pages DSL's `hostPanel()` primitive, referenced by registered panel name.

## 5. Developer Experience — From Zero to Running

The ops perspective solves the "first 10 minutes" problem. A developer evaluating CaseHub should feel powerful immediately.

### Pure YAML — the fastest path

1. Clone an example YAML project repo (or create one from a template)
2. Point the ops perspective at the git repo
3. Ops perspective detects the archetype (pure YAML — no `pom.xml`, just YAML files)
4. Provisions infrastructure: PostgreSQL container (Podman), scaffold container with YAML mounted
5. App is running. Semantic views show the full designed state, agent topology, channel wiring.
6. Developer edits YAML in their IDE, commits — ops perspective detects the change (git poll or webhook), re-converges

No Maven. No JDK. No Quarkus configuration. The developer sees their case definitions, agents, and channels running as a live system within minutes.

### Java/Hybrid — the build path

1. Clone the app repo (e.g. fsitrading)
2. Point the ops perspective at the git repo (or provide GAV coordinates from a Maven registry)
3. Ops perspective detects the archetype (Java — has `pom.xml`)
4. Builds the project (`mvn package`), provisions infrastructure, deploys the JAR container
5. App is running. Runtime observability shows health, drift, case instances, agent status.
6. Developer iterates through the normal build cycle — ops perspective redeploys on new artifacts

### Shared scaffold — the multi-app path

1. Developer has 2-3 pure-YAML projects
2. Points ops perspective at all of them
3. Ops perspective offers: deploy each separately (dedicated) or co-host in a shared scaffold
4. If shared: single scaffold container, multiple YAML apps loaded with namespace isolation
5. All apps visible in the fleet dashboard, each with its own semantic views

### Dev mode specifics (Podman)

- Ops perspective itself runs locally (developer starts scaffold with the ops perspective YAML)
- Deploys managed apps to Podman containers on the local machine
- PostgreSQL containers provisioned automatically per deployment (or shared for co-hosted YAML apps)
- Port mapping for each deployed app's UI and REST/GraphQL endpoints
- Logs streamed to the ops perspective's monitoring views

## 6. Ops Perspective UI — Tab Structure and Data Flow

The ops perspective's UI is defined in its `app.yaml` using the casehub-pages DSL.

### Primary tabs

| Tab | Purpose | Key views |
|-----|---------|-----------|
| **Fleet** | Overview of all managed deployments | Deployment cards with health status, archetype badge (YAML/Java/Hybrid), convergence state. Click through to deployment detail. |
| **Deploy** | Provision a new deployment | Git repo URL input, archetype auto-detection, topology choice (dedicated/shared), infrastructure config. Kicks off a provisioning case. |
| **Health** | Cross-fleet health monitoring | Aggregated health across all deployments. Drill down per deployment. Health history timeline. |
| **Drift** | Infrastructure drift across fleet | Infrastructure-level drift: container stopped, wrong image, port mismatch, volume missing. Sources from ops perspective's own `ops-container` ReconciliationLoop. For YAML apps: design-vs-actual overlay using YAML-parsed topology. Application-level drift (agent not registered, channel misconfigured) is shown in the deployment detail view, sourced from the deployed app. |
| **Situations** | Active and resolved situations | RAS situation list. Active situations with resolution options. CBR-suggested resolutions. Resolution history feeding back to CBR. |
| **Updates** | Version management across fleet | Available updates per deployment. Rollout strategy (canary/rolling). Update history. Rollback capability. |

### Deployment detail view

Drill-down from Fleet tab with archetype-dependent sub-tabs:

| Sub-tab | Pure YAML | Java/Hybrid | Data source |
|---------|-----------|-------------|-------------|
| **Overview** | Health + designed agent topology graph | Health + runtime topology | Design-time (YAML) + Runtime (proxied) |
| **Agents** | Eidos org chart + capability health status | Eidos agent registry + capability health | Design-time (YAML) + Runtime (proxied) |
| **Channels** | Channel topology diagram + message flow | Runtime channel state | Design-time (YAML) + Runtime (proxied) |
| **Cases** | Case definition tree + active instances | Active case instances | Design-time (YAML) + Runtime (proxied) |
| **Manifests** | LLM provider/alias config table | Runtime model availability | Design-time (YAML parsed) |
| **Infrastructure** | Container topology + DB/qhorus/LLM backing | Container topology + DB/qhorus/LLM backing | Local (ops perspective state) |
| **Drift (app)** | Application-level drift: agent/channel/case-type convergence | Application-level convergence status | Runtime (proxied from deployed app) |
| **CBR** | Resolution history for this deployment | Resolution history for this deployment | Local (ops perspective case history) |

The **Infrastructure** sub-tab shows the ops perspective's own DesiredState: the container topology graph (`ContainerNodeSpec` hierarchy — app containers, database containers, networks, volumes) and supporting infrastructure status. This is local data from the ops perspective's `ops-container` ReconciliationLoop.

The **Overview** and **Agents/Channels/Cases** sub-tabs show the deployed app's internal topology. For pure-YAML apps, design-time data comes from YAML parsing (structural view) and runtime data comes from the deployed app via `DeploymentDataProxy` (convergence status, health). For Java/hybrid apps, only runtime data is available (proxied from the deployed app).

The Agents sub-tab shows what eidos provides: agent identity (`AgentDescriptor`), capabilities (`AgentCapability`), supervision/delegation/escalation relationships (org model), and capability health (`CapabilityHealth`). Live agent session state (is the agent actively processing? what plan items?) requires Claudony integration (deferred, §7). The eidos-level view is already useful — it answers "what agents exist, what can they do, and are their capabilities healthy?"

### Data flow

```
                          INFRASTRUCTURE LAYER (ops perspective)
                          ═══════════════════════════════════════
Git repo ──pull──▶ Ops perspective ──ContainerGoalCompiler──▶ ops-container ReconciliationLoop
                        │                                              │
                        │                                        ┌─────┴─────┐
                        │                                        ▼           ▼
                        │                                  Podman/K8s    PodmanActualState
                        │                                   targets       Adapter
                        │                                        │
                        │                                        ▼
                        │                                  Deployed apps
                        │                                  (scaffold or
                        │                                   custom Quarkus)
                        │                                        │
                        │           APPLICATION LAYER            │
                        │           ═════════════════            │
                        │                                        ▼
                        │                                  DeploymentGoalCompiler
                        │                                  (runs IN deployed app)
                        │                                        │
                        │                                  agents, channels,
                        │                                  case types, trust
                        │                                        │
                        ▼                                        ▼
                   Pages DSL ◀─── DeploymentDataProxy ───▶ Deployed app REST
                   renders UI     (routes by topology:      endpoints (auto-
                        ▲          shared → CDI,            discovered by CDI)
                        │          dedicated → HTTP)
                        │
                   Local data:
                   YAML parsing (design-time)
                   + ops-app REST (fleet state)
```

## 7. Claudony Integration (Deferred)

Deferred until the worker pool model settles. Target integration surface: Claudony's session-list and terminal components for deployed-app agent observability. The Trellis precedent shows Claudony's primitives are reusable — the ops perspective is another consumer, not a replacement. In the interim, link-out to Claudony's own UI provides agent session visibility.

## 8. Platform Gaps to Address

The following gaps were identified during platform investigation and must be addressed for the ops perspective to work:

| Gap | Current state | Required | Priority | Gated by |
|-----|--------------|----------|----------|----------|
| **No perspective concept in scaffold** | Flat module detection, conditional tabs | `app.yaml` loading and module activation | High — foundational | — |
| **Podman provisioning module** | No container DesiredState types exist | `ops-container` module with ContainerNodeSpec hierarchy, PodmanActualStateAdapter, PodmanNodeProvisioner | High — required for local dev mode | — |
| **No pure-YAML consumer project** | Plugin model exists only in test fixtures | At least one reference YAML project. Uses existing engine YAML DSL + `casehub-deployment.yaml` topology declarations for MVP. Full YAML DesiredState graphs require DesiredState C10 (Declaration Surfaces, status: 📋 planned) | High — needed for dev experience story | DesiredState C10 for full experience |
| **blocks-ui semantic viewers** | Existing diagram components unknown extent | Graph viewer, org chart, channel topology, case structure | High — core of the enhanced YAML experience | — |
| **LLM manifests not adopted** | agent-config.yaml is 5 days old, zero consumers | Example apps updated with manifests first, THEN manifest table component | Medium — sequenced: adoption before tooling | Manifest adoption |
| **Ops deployment module is Java-only** | No plugin YAML for deployment node types | YAML plugin definitions for ops deployment nodes (optional — Java works) | Medium — required only for YAML-defined ops extensions | DesiredState C10 |
| **Examples not updated** | fsitrading doesn't use manifests or plugin model | Reference examples using latest platform features | Medium — needed for credible demos | — |

**Key dependency: DesiredState C10 (Declaration Surfaces).** The full pure-YAML experience — where YAML projects define arbitrary DesiredState graphs — depends on the YAML→`GraphDescriptor` pipeline in DesiredState C10 (status: 📋 planned). The MVP does NOT require C10: pure-YAML projects in the MVP use the existing engine YAML DSL for case definitions and `casehub-deployment.yaml` topology declarations for agent/channel topology. C10 unlocks Phase 3 (§9).

## 9. MVP and Phasing

### Phase 1 — MVP: Single Java app, Podman, health monitoring

The smallest useful version. Demonstrates the ops perspective concept end-to-end:

- **Perspective loading** — scaffold reads `app.yaml`, activates declared modules, renders the ops UI
- **Single deployment** — deploy one Java app (e.g. fsitrading) to a Podman container on the developer's machine
- **Infrastructure provisioning** — PostgreSQL container provisioned alongside the app, port mapping configured
- **Health monitoring** — health check polling, health status in Fleet tab
- **Manual reconciliation** — drift detection with manual "re-converge" action
- **Case-managed provisioning** — the provisioning flow runs as a case on the engine (using existing `ApplicationCaseDescriptor`)

**Not in Phase 1:** Pure-YAML projects, shared scaffold, cross-deployment CBR, semantic views, multiple deployments, K8s target, auto-remediation.

**Gates Phase 2:** Perspective concept proven, Podman provisioning module working, case-managed provisioning demonstrated.

### Phase 2 — Fleet: Multiple deployments, drift auto-remediation, baseline CBR

- **Multiple deployments** — Fleet tab managing 2+ apps (mix of Java and pure-YAML)
- **Pure-YAML projects** — scaffold as YAML runner using existing engine YAML DSL + `casehub-deployment.yaml` topology declarations (no C10 dependency)
- **Drift auto-remediation** — `DriftRemediationCaseDescriptor` integrated, severity classification, auto-remediate benign drift
- **Scaling** — `ScalingEventCaseDescriptor` integrated, RAS-driven scaling events
- **Incident response** — `IncidentResponseCaseDescriptor` integrated, fault policy escalation
- **Baseline CBR** — per-deployment case history, initial cross-deployment retrieval
- **Semantic views** — graph viewer and org chart components for pure-YAML projects

**Gates Phase 3:** Fleet management proven across archetypes, CBR value demonstrated.

### Phase 3 — Full experience: Shared scaffold, cross-deployment CBR, full semantic views

- **Shared scaffold** — multiple YAML apps co-hosted with DesiredState tenancy isolation
- **Cross-deployment CBR** — full feature-engineered retrieval across deployment archetypes and infrastructure types
- **All semantic views** — manifest table (after manifest adoption), channel topology, case structure
- **K8s production target** — reuse ops/app K8s infrastructure (`KubernetesActualStateAdapter`, etc.)
- **Full YAML DesiredState graphs** — depends on DesiredState C10 landing
- **Environment-scoped instances** — dev/staging/prod topology with approval workflow differentiation

## References

- `/Users/mdproctor/claude/casehub/scaffold` — scaffold module structure, console architecture (10 view files, ~200 lines TS)
- `/Users/mdproctor/claude/casehub/platform` — yaml-core primitives (VariableResolver, ForEachExpander, Module system)
- `/Users/mdproctor/claude/casehub/desiredstate` — convergence engine (GoalCompiler → ActualStateAdapter → TransitionPlanner → TransitionExecutor → ReconciliationLoop), plugin model, 5 declaration surfaces
- `/Users/mdproctor/claude/casehub/ops` — 6 domain modules (deployment, infra, compliance, iot, app, testing), adaptive topology, drift checkers
- `/Users/mdproctor/claude/casehub/engine` — case lifecycle, CBR (Retrieve/Reuse/Revise/Retain), REST/GraphQL/MCP surface
- `/Users/mdproctor/claude/casehub/eidos` — AgentDescriptor, VocabularyRegistry, AgentRegistry, CapabilityHealth, org model, YAML-driven registration
- `/Users/mdproctor/claude/casehub/qhorus` — embedded agent mesh, speech-act typed messages, typed channels, PostgreSQL backing
- `/Users/mdproctor/claude/casehub/claudony` — session management, terminal streaming, CaseHub SPI implementations, Trellis precedent
- `/Users/mdproctor/claude/casehub/parent/docs/PLATFORM.md` — 4-tier architecture (Foundation → Orchestration → Integration → Application), boundary rules
- casehubio/scaffold#52 — ops perspective issue
- HANDOFF.md — initial brief, design questions, investigation plan
- `specs/issue-52-ops-perspective/decisions.md` — 12 validated decisions (D1–D12)
- `specs/issue-52-ops-perspective/platform-investigation.md` — platform investigation synthesis
