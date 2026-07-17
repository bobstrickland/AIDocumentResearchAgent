package claudeagent.agent.guardrails;

public class AgentIterationLimitExceededException extends Exception {

	private static final long serialVersionUID = -8434816434958678736L;

	public AgentIterationLimitExceededException(boolean isError, int maxCalls) {
		super(isError
				? "Exceeded maximum tool call errors (" + maxCalls +") - agent appears stuck retrying a failing operation"
				: "Exceeded maximum tool calls (" + maxCalls + ")"
				);
	}
	public AgentIterationLimitExceededException(boolean isError, int maxCalls, Exception e) {
		super(isError
				? "Exceeded maximum tool call errors (" + maxCalls +") - agent appears stuck retrying a failing operation"
				: "Exceeded maximum tool calls (" + maxCalls + ")"
				, e);
	}

}
