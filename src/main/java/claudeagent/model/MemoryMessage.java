package claudeagent.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.messages.MessageType;

public class MemoryMessage implements org.springframework.ai.chat.messages.Message {

	private final String text;
	private final Map<String, Object> metadata;
	private final MessageType messageType;
    private final Long id;
	
	public MemoryMessage (String text, MessageType messageType, Long id) {
		super();
		this.messageType = messageType;
		this.text = text;
		this.metadata = new HashMap<String, Object>();
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	@Override
	public String getText() {
		return text;
	}

	@Override
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	@Override
	public MessageType getMessageType() {
		return messageType;
	}

}
