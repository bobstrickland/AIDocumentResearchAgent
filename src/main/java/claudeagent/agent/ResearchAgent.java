package claudeagent.agent;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

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
		List<Message> conversationalMemory = new ArrayList<Message>();

		List<MemoryMessage> recent = memoryService.getRecentMessages(sessionId);
		List<MemoryMessage> context = memoryService.getRecentMessages(sessionId);

		if (recent != null) {
			conversationalMemory.addAll(recent);
		}
		if (context != null) {
			conversationalMemory.addAll(context);
		}
		String chatClientResponse =  chatClient.prompt().messages(conversationalMemory).user(goal).call().content();
		memoryService.store(MessageType.USER, sessionId, goal);
		memoryService.store(MessageType.ASSISTANT, sessionId, chatClientResponse);
		return chatClientResponse;
	}
	

}
