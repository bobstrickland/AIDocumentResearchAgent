package claudeagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class ResearchAgent {
	
	private final ChatClient chatClient;
	
	public  ResearchAgent(ChatClient.Builder builder, ResearchTools tools) {
		this.chatClient = builder.defaultTools(tools).build();
	}
	
	public String run (String goal) {
		return chatClient.prompt().user(goal).call().content();
	}
	

}
