package claudeagent.documentImport;

import org.springframework.stereotype.Service;

@Service
public interface DocumentStoreInterface {
	
	public String putDocument(byte[] byteArray);
	
	public byte[] getDocument(String documentKey);
	
}
