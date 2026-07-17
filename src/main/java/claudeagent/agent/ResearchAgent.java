package claudeagent.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import claudeagent.model.MemoryMessage;


@Service
public class ResearchAgent {
	
	private final ChatClient chatClient;
	
	private final MemoryService memoryService;
	
	public  ResearchAgent(ChatClient.Builder builder, ResearchTools tools, MemoryService memoryService) {
		this.memoryService = memoryService;
		this.chatClient = builder
			    .defaultSystem("You are a research assistant. Use the search and retrieval " +
			                    "tools available to you to answer questions grounded in the " +
			                    "document collection. Always cite which documents you drew from.")
			    .defaultTools(tools)
			    .build();
	}
	
	public String run (String goal, String sessionId) {
		List<Message> conversationalMemory = new ArrayList<Message>();

		List<MemoryMessage> recent = memoryService.getRecentMessages(sessionId, 5);
		List<MemoryMessage> context = memoryService.getRecentMessages(sessionId, 5);
		

		if (recent != null)
		conversationalMemory.addAll(recent);
		if (context != null)
		conversationalMemory.addAll(context);
		String chatClientResponse =  chatClient.prompt().messages(conversationalMemory).user(goal).call().content();
		memoryService.store(MessageType.USER, sessionId, goal);
		memoryService.store(MessageType.ASSISTANT, sessionId, chatClientResponse);
		return chatClientResponse;
	}
	

}
