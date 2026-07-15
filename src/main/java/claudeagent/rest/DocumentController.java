package claudeagent.rest;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ai.djl.translate.TranslateException;
import claudeagent.documentImport.DocumentChunker;
import claudeagent.documentImport.DocumentEmbedder;
import claudeagent.documentImport.DocumentStoreInterface;
import claudeagent.documentImport.fileHandlers.PdfFileHandler;
import claudeagent.model.DocumentChunk;
import claudeagent.model.DocumentSearchResult;
import claudeagent.model.repository.DocumentChunkRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentController {


	@Value("${UPLOAD_DIR:./uploads/}")
	private String UPLOAD_DIR;
	
	@Autowired
	DocumentStoreInterface documentStore;

	@Autowired
	DocumentEmbedder documentEmbedder;

	@Autowired
	DocumentChunkRepository documentChunkRepository;
	
	@GetMapping(value="/query")
	public List<DocumentSearchResult> queryFile(@RequestParam("query") String queryString, @RequestParam(value="limit", defaultValue="5") int topk) {
		String embeddedFloatString;
		try {
			float[] embeddings = documentEmbedder.embedQuery(queryString);
			embeddedFloatString = Arrays.toString(embeddings);
			
		} catch (Exception e) {
			embeddedFloatString = null;
			e.printStackTrace();
		}
		List<DocumentChunk> documentChunkList = documentChunkRepository.findNearest(embeddedFloatString, topk);
		if (documentChunkList != null && !documentChunkList.isEmpty()) {
			
			List<DocumentSearchResult> resultList = documentChunkList.stream()
			.map(chunk -> new DocumentSearchResult(chunk.getDocumentId(), chunk.getSourceTitle(), chunk.getContent()))
			.toList();
			return resultList;
		} else {
			return null;
		}
	}
	
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
		
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
        	String key = documentStore.putDocument(file.getBytes());
            String text;
            if ("application/pdf".equalsIgnoreCase(file.getContentType())) {
            	text = PdfFileHandler.extractText(file.getBytes());
            } else {
            	text = new String(file.getBytes());
            }
            String title = file.getOriginalFilename();
            List<String> chunky = DocumentChunker.chunkText(text, 768, 115);
            Instant rightNow = Instant.now();
            
            int chunkIndex = 0;
            int errorCount = 0;
    		for (String chunk: chunky) {
    			float[] embeddings;
				try {
					embeddings = documentEmbedder.embed(chunk);
	    			DocumentChunk documentChunk = new DocumentChunk();
	    			documentChunk.setEmbedding(embeddings);
	    			documentChunk.setDocumentId(key);
	    			documentChunk.setChunkIndex(chunkIndex++);
	    			documentChunk.setContent(chunk);
	    			documentChunk.setSourceTitle(title);
	    			documentChunk.setCreatedAt(rightNow);
//	    			documentChunk.setSourceUrl(sourceURL);
//	    			documentChunk.setTags(tags);

	    			documentChunkRepository.save(documentChunk);
				} catch (TranslateException e) {
					errorCount ++;
					e.printStackTrace();
				}
    		}
    		documentChunkRepository.flush();
            
            if (errorCount > 0) {
            	title += " ["+errorCount+"] errors ";
            }
            if (chunkIndex > 0) {
            	title += " ["+chunkIndex+"] Chunks Saved ";
            }

            return ResponseEntity.ok("File uploaded successfully:\n" + title);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload the file: " + e.getMessage());
        }
	}
	
	
	
}
