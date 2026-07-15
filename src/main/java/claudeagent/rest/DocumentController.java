package claudeagent.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import claudeagent.demo.ResearchAgent;
import claudeagent.documentImport.DocumentChunker;
import claudeagent.documentImport.PdfFileHandler;
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
	
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
		
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            String text;
            if ("application/pdf".equalsIgnoreCase(file.getContentType())) {
            	text = PdfFileHandler.extractText(file.getBytes());
            } else {
            	text = new String(file.getBytes());
            }
            
            List<String> chunky = DocumentChunker.chunkText(text, 768, 115);
            
StringBuilder sb = new StringBuilder();
    		for (String s: chunky) {
    			sb.append(s+"\n---------------------------------------------------");
    		}
            
            
            
            System.out.println (file.getContentType());
            
            

            return ResponseEntity.ok("File uploaded successfully:\n" + sb.toString());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload the file: " + e.getMessage());
        }
	}
	
	
	
}
