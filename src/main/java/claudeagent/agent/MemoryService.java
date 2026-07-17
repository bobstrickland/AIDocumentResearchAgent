package claudeagent.agent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ai.djl.translate.TranslateException;
import claudeagent.documentImport.DocumentEmbedder;
import claudeagent.model.ConversationHistory;
import claudeagent.model.MemoryMessage;
import claudeagent.model.repository.ConversationHistoryRepository;
import lombok.Getter;
import lombok.Setter;

@Service
@Getter @Setter
public class MemoryService {
	private static final Logger log = LoggerFactory.getLogger(MemoryService.class);
	
	private final ConversationHistoryRepository conversationHistoryRepository;
	
	private final DocumentEmbedder documentEmbedder;
	private final int recentMessageLimit;
	private final int contextMessageLimit;
	
	

	public MemoryService(ConversationHistoryRepository conversationHistoryRepository, 
			             DocumentEmbedder documentEmbedder,
			             @Value("${agent.memory.limit.recent:5}") int recentMessageLimit, 
			             @Value("${agent.memory.limit.context:5}") int contextMessageLimit) {
		super();
		this.conversationHistoryRepository = conversationHistoryRepository;
		this.documentEmbedder = documentEmbedder;
		this.recentMessageLimit = recentMessageLimit;
		this.contextMessageLimit = contextMessageLimit;
	}

	public List<MemoryMessage> getRecentMessages(String sessionId) {
		return getRecentMessages(sessionId, recentMessageLimit);
	}

	public List<MemoryMessage> getContextMessages(String goal, String sessionId) {
		return getContextMessages(goal, sessionId, contextMessageLimit);
	}

	public List<MemoryMessage> getRecentMessages(String sessionId, int limit) {
		List<ConversationHistory> conversationHistoryList = conversationHistoryRepository.findMostRecent(sessionId, limit);
		if (conversationHistoryList != null && !conversationHistoryList.isEmpty()) {
			
			List<MemoryMessage> resultList = conversationHistoryList.stream()
			.map(memory -> new MemoryMessage(memory.getContent(), MessageType.valueOf(memory.getMessageType()), memory.getId()))
			.toList();
			Collections.reverse(resultList); // reverse list so it's returned to the agent in 
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
				.map(memory -> new MemoryMessage(memory.getContent(), MessageType.valueOf(memory.getMessageType()), memory.getId()))
				.toList();
				return resultList;
			} else {
				return null;
			}
		} catch (TranslateException e) {
			log.error("Exception translating context messages",e);
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
			log.error("Exception translating store",e);
		}
	}

	
}
