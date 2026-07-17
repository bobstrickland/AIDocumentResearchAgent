package claudeagent.agent;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;

import ai.djl.translate.TranslateException;
import claudeagent.documentImport.DocumentEmbedder;
import claudeagent.model.ConversationHistory;
import claudeagent.model.MemoryMessage;
import claudeagent.model.repository.ConversationHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter @Setter @AllArgsConstructor @Builder
public class MemoryService {
	
	private final ConversationHistoryRepository conversationHistoryRepository;
	
	private final DocumentEmbedder documentEmbedder;

	public List<MemoryMessage> getRecentMessages(String sessionId, int limit) {
		List<ConversationHistory> conversationHistoryList = conversationHistoryRepository.findMostRecent(sessionId, limit);
		if (conversationHistoryList != null && !conversationHistoryList.isEmpty()) {
			
			List<MemoryMessage> resultList = conversationHistoryList.stream()
			.map(memory -> new MemoryMessage(memory.getContent(), MessageType.valueOf(memory.getMessageType())))
			.toList();
			return resultList;
		} else {
			return null;
		}
	}

	public List<MemoryMessage> getContextMessages(String goal, String sessionId, int limit) {
		try {
			float[] embeddings = documentEmbedder.embedQuery(goal);
			String embeddedFloatString = Arrays.toString(embeddings);
			List<ConversationHistory> conversationHistoryList = conversationHistoryRepository.findNearest(embeddedFloatString, sessionId, limit);
			if (conversationHistoryList != null && !conversationHistoryList.isEmpty()) {
				
				List<MemoryMessage> resultList = conversationHistoryList.stream()
				.map(memory -> new MemoryMessage(memory.getContent(), MessageType.valueOf(memory.getMessageType())))
				.toList();
				return resultList;
			} else {
				return null;
			}
		} catch (TranslateException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void store(MessageType messageType, String sessionId, String text) {
		try {
			float[] embeddings = documentEmbedder.embed(text);
			ConversationHistory conversationHistory = new ConversationHistory();
			conversationHistory.setMessageType(messageType.getValue());
			conversationHistory.setContent(text);
			conversationHistory.setSessionId(sessionId);
			conversationHistory.setEmbedding(embeddings);
			conversationHistoryRepository.saveAndFlush(conversationHistory);
		} catch (TranslateException e) {
			e.printStackTrace();
		}
	}
	
}
