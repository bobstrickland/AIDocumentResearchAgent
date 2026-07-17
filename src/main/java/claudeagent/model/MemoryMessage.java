package claudeagent.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.messages.MessageType;

public class MemoryMessage implements org.springframework.ai.chat.messages.Message {

	private String text;
	private Map<String, Object> metadata;
	private MessageType messageType;
	
	public MemoryMessage (String text, MessageType messageType) {
		super();
		this.messageType = messageType;
		this.text = text;
		this.metadata = new HashMap<String, Object>();
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
