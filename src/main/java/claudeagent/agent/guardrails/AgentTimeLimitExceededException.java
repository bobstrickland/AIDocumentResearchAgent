package claudeagent.agent.guardrails;

public class AgentTimeLimitExceededException extends Exception {

	private static final long serialVersionUID = 5873166127593380578L;

	public AgentTimeLimitExceededException() {
		super("Agent exceeded maximum allowed run time");
	}


	public AgentTimeLimitExceededException(Exception e) {
		super("Agent exceeded maximum allowed run time", e);
	}

	
	
}
