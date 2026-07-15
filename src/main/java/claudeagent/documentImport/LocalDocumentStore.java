package claudeagent.documentImport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalDocumentStore implements DocumentStoreInterface {



	@Value("${UPLOAD_DIR:./uploads/}")
	private String UPLOAD_DIR;
	
	
	@Override
	public String putDocument(byte[] byteArray) {

        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = "document_"+Instant.now().toEpochMilli();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            
            Files.write(path, byteArray);

            return fileName;

        } catch (IOException e) {
            return null;
        }
	}


	@Override
	public byte[] getDocument(String documentKey) {
		byte[] byteArray = null;
        try {
            File file = new File(UPLOAD_DIR + documentKey);
            if (file.exists()) {
            	byteArray = Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
        }
        return byteArray;
	}

}
