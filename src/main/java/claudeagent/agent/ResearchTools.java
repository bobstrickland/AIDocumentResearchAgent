package claudeagent.agent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import claudeagent.documentImport.DocumentEmbedder;
import claudeagent.documentImport.DocumentStoreInterface;
import claudeagent.model.DocumentSearchResult;
import claudeagent.model.repository.DocumentChunkRepository;
import claudeagent.rest.DocumentController;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Service
@Getter @Setter @AllArgsConstructor
public class ResearchTools {


	@Autowired
	private final DocumentController documentController;
	
	@Autowired
	private final DocumentStoreInterface documentStore;


//	@Value("${OUTPUT_DIR:./output/}")
//	private String outputDirectory;

	
	
	@Tool (description="Search the document vector database for a given text query")
	public List<DocumentSearchResult> SearchDocumentLibrary(String text, int limit) {
		List<DocumentSearchResult> searchResults = documentController.queryFile(text, limit);
		return searchResults;
	}
	
	public byte[] pullDocumentById (String documentId) {
		byte[] byteArray = documentStore.getDocument(documentId);
		return byteArray;
	}
	
	public void writeFileData(String fileName, byte[] fileData) {
		System.out.println("Saving "+fileName);
		Path path = Paths.get("./output/"+fileName);
        try {
			Files.write(path, fileData);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	

	
	@Tool (description="Finds a document based on document ID and writes it to disc")
	public void writeDocuemtnToDisc(String documentId, String fileName) {
		byte[] fileData = pullDocumentById (documentId); 
		writeFileData(fileName, fileData);
	}
	
}
