package claudeagent.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import claudeagent.agent.ResearchAgent;
import claudeagent.agent.guardrails.IterationGuard;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/agent")
@Getter @Setter @AllArgsConstructor @Builder
public class AgentController {

	private final ResearchAgent agent;
	private final IterationGuard guard;
	private static final Logger log = LoggerFactory.getLogger(AgentController.class);

	@PostMapping("/run")
	public String runAgent(@RequestBody String goal, HttpSession session) {
		String sessionId = session==null?null:session.getId();
        SessionContext.setCurrentSessionId(sessionId);
        String rv = null;
        try {
        	guard.startAgent(sessionId);
        	rv =  agent.run(goal, sessionId);
        } finally {
        	guard.reset(sessionId);
        	SessionContext.clearCurrentSessionId();
        }
        return rv;
		
	}
	
}
