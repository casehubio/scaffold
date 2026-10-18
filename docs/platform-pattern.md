# CaseHub App Platform Pattern

Scaffold serves as the CaseHub app platform — a reusable Quarkus application
that hosts domain-specific "apps" defined by descriptor files.

## App Descriptor (app.yaml)

Each app is defined by a `META-INF/app.yaml` on the classpath:

```yaml
app:
  name: ops
  description: "CaseHub operations management"
  version: "1.0"

  modules:
    required: [engine, desiredstate, work]
    optional: [ops-service, ops-deployment, ops-compliance]

  views:
    tabs:
      - id: fleet
        label: "Fleet"
        view: fleet-dashboard

  datasets:
    topology:
      endpoint: /api/ops/topology
      source: topology-service

  connections:
    reconciliation-events:
      endpoint: /api/ops/reconciliation/events
      type: sse
      source: reconciliation-service
```

**Sections:**
- `modules` — required modules must be on the classpath; optional modules are
  detected via CDI classpath probing
- `views.tabs` — each tab maps an `id` and `label` to a `view` key registered
  in the frontend `VIEW_REGISTRY`
- `datasets` — REST-polled data sources bound to pages-ui data tables
- `connections` — SSE push channels for live event streaming

The `AppLoader` class parses this YAML and produces an `AppConfig` record.
`AppProducer` is a CDI bean that loads the config from the classpath at startup.
`AppResource` serves it at `GET /api/app`.

## Module Activation

`ModuleRegistryResource` determines which modules are active. When an app
descriptor is present, it uses the declared required/optional lists. For optional
modules, it probes the classpath:

```java
case "ops-service" -> "io.casehub.ops.service.api.OpsApplicationApi";
case "desiredstate" -> "io.casehub.desiredstate.api.GoalCompiler";
```

When no app descriptor is present, it falls back to probing a hardcoded list.

## Service Interface Pattern

Domain services use a dual-implementation pattern for embedded vs distributed
deployment:

1. **Interface** in `io.casehub.flow.service` — defines the contract
2. **Embedded implementation** in `io.casehub.flow.service.embedded` —
   `@Alternative @Priority(100)`, activated when the domain module is on the
   classpath as a CDI bean
3. **Client implementation** in `io.casehub.flow.service.client` —
   `@DefaultBean`, used when the embedded impl is not available; calls remote
   REST/GraphQL endpoints

Current service interfaces:
- `TopologyService` — `getSnapshot()` returns `TopologySnapshot`
- `DeploymentService` — `availableDefinitions()`, `availableTargets()`,
  `deploy()`, `status()`
- `ReconciliationService` — `activeLoops()`, `getDrift(loopId)`

## Frontend View Registration

Views are TypeScript modules in `src/views/` using the `@casehubio/pages-ui`
DSL. Each exports a view constant (e.g., `fleetView`).

Register views in `VIEW_REGISTRY` in `src/index.ts`:

```typescript
const VIEW_REGISTRY: Record<string, unknown> = {
  "fleet-dashboard": fleetView,
  "topology-graph": topologyView,
  // ...
};
```

The `view` field in `app.yaml` tabs must match a key in this registry.

## SSE Event Streaming

Live event channels use Server-Sent Events:

1. **Backend:** `ReconciliationEventResource` at
   `/api/ops/reconciliation/events` observes CDI `CloudEvent` events and
   streams them to SSE subscribers
2. **Frontend:** `events.ts` connects via `EventSource` and dispatches parsed
   events on the pages event bus
3. **Components:** The `<casehub-topology-graph>` web component listens for
   `reconciliation-event` to update node statuses in real-time

## Creating a New App

1. Create `META-INF/app.yaml` with the app descriptor
2. Add module probe classes to `ModuleRegistryResource.resolveProbeClass()`
3. Create service interfaces with embedded/client dual implementations
4. Create frontend views and register them in `VIEW_REGISTRY`
5. Add datasets and connections to `app.yaml`
