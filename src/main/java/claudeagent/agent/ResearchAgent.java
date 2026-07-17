package claudeagent.agent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.stereotype.Service;

import claudeagent.agent.guardrails.AgentIterationLimitExceededException;
import claudeagent.agent.guardrails.AgentTimeLimitExceededException;
import claudeagent.model.MemoryMessage;


@Service
public class ResearchAgent {
	private static final Logger log = LoggerFactory.getLogger(ResearchAgent.class);
	
	private final ChatClient chatClient;
	
	private final MemoryService memoryService;
	
	public  ResearchAgent(ChatClient.Builder builder, ResearchTools tools, MemoryService memoryService) {
		this.memoryService = memoryService;
		this.chatClient = builder
			    .defaultSystem("You are a research assistant. "
			    		     + "Use the search and retrieval tools available to you to answer questions grounded in the document collection. "
			    		     + "Remain within the document collection's scope. "
			                 + "Always cite which documents you drew from.")
			    .defaultTools(tools)
			    .build();
	}
	
	public String run (String goal, String sessionId) {
		List<MemoryMessage> combinedMessageList = new ArrayList<MemoryMessage>();

		List<MemoryMessage> recent = memoryService.getRecentMessages(sessionId);
		List<MemoryMessage> context = memoryService.getContextMessages(goal, sessionId);

		if (recent != null) {
			combinedMessageList.addAll(recent);
		}
		if (context != null) {
			combinedMessageList.addAll(context);
		}
		List<MemoryMessage> conversationalMemory = combinedMessageList.stream()
			      .collect(Collectors.toMap(MemoryMessage::getId, m -> m, (a, b) -> a, LinkedHashMap::new))
			      .values().stream()
			      .sorted(Comparator.comparingLong(MemoryMessage::getId)) // sort messages in chronological order (via id smallest to largest)
			      .collect(Collectors.toList());
		
		String chatClientResponse;
		try {
			chatClientResponse =  chatClient.prompt().messages(new ArrayList<Message>(conversationalMemory)).user(goal).call().content();
		} catch (ToolExecutionException tee) {
	          Throwable cause = tee.getCause();
	          if (cause instanceof AgentIterationLimitExceededException || cause instanceof AgentTimeLimitExceededException) {
	              log.warn("Agent run terminated by guardrail for session {}: {}", sessionId, cause.getMessage());
	              memoryService.store(MessageType.USER, sessionId, goal);
	              return "Run terminated: " + cause.getMessage();
	          }
	          throw tee;
		}

		memoryService.store(MessageType.USER, sessionId, goal);
		memoryService.store(MessageType.ASSISTANT, sessionId, chatClientResponse);
		return chatClientResponse;
	}
	

}
