package claudeagent.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import claudeagent.model.DocumentSearchResult;
import claudeagent.service.DocumentService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/document")
@Getter @Setter @Builder
public class DocumentController {
	private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	private final DocumentService documentService;
	
	@GetMapping(value="/query")
	public List<DocumentSearchResult> xqueryFile(@RequestParam("query") String queryString, @RequestParam(value="limit", defaultValue="5") int topk) {
		return documentService.queryFile(queryString, topk);
	}

	@GetMapping(value="/findDocument")
	public List<DocumentSearchResult> xfindDocumentById(@RequestParam("documentId") String documentId, @RequestParam(value="limit", defaultValue="0") int limit) {
		return documentService.findDocumentById(documentId, limit);
	}
	
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> xuploadFile(@RequestParam("file") MultipartFile file) {
		return documentService.uploadFile(file);
	}
	
	
	
}
