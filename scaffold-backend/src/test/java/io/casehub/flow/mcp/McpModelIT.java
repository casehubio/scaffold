package io.casehub.flow.mcp;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("platform.mcp.CaseHubMcpTools not on classpath — re-enable after platform upgrade")
class McpModelIT {

    @Test void tier0ListsAllFourDomains() {}
    @Test void tier0IncludesEnricherSummaries() {}
    @Test void tier1EngineHasExpectedOperations() {}
    @Test void tier1QhorusHasExpectedOperations() {}
    @Test void casehubActionDispatchesToEngine() {}
    @Test void casehubActionRejectsUnknownDomain() {}
}
