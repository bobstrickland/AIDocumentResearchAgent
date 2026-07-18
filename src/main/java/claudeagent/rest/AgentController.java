package claudeagent.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import claudeagent.agent.ResearchAgent;
import claudeagent.agent.guardrails.IterationGuard;
import claudeagent.model.ConversationHistory;
import claudeagent.service.MemoryService;
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
	private final MemoryService memoryService;
	private static final Logger log = LoggerFactory.getLogger(AgentController.class);

	@PostMapping("/run")
	public String runAgent(@RequestBody String goal, HttpSession session) {
		String sessionId = session==null?null:session.getId();
        SessionContext.setCurrentSessionId(sessionId);
        String rv = null;
        try {
        	guard.startAgent(sessionId);
        	rv =  agent.run(goal, sessionId)+"\n";
        } finally {
        	guard.reset(sessionId);
        	SessionContext.clearCurrentSessionId();
        }
        return rv;
	}

	@PostMapping("/session/clear")
	public String clearSession(HttpSession session) {
		String sessionId = session==null?null:session.getId();
		memoryService.clear(sessionId);
        return "Conversation History for session ["+sessionId+"] cleared.\n";
	}

	@GetMapping("/session/conversation")
	public String listMemorySession(HttpSession session) {
		String sessionId = session==null?null:session.getId();
		List<ConversationHistory> conversationList = memoryService.getAllMessages(sessionId);
		StringBuffer sb = new StringBuffer();
		conversationList.stream().forEach(memory -> sb.append("\n[").append(memory.getMessageType()).append("]")
				                                      .append(" Tokens [").append(memory.getInputTokens()).append(" in / ")
				                                      .append(memory.getOutputputTokens()).append(" out]\n")
				                                      .append(memory.getContent()).append("\n") );
        return sb.toString();
	}
	
}
