package claudeagent.model;

import java.time.Instant;
import java.util.List;

public record ReportSearchResult(String agentGoal,
		String reportText,
		List<String> sourceDocumentIds,
		Instant createdAt) {

}
