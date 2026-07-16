package claudeagent.agent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import claudeagent.documentImport.DocumentStoreInterface;
import claudeagent.model.DocumentSearchResult;
import claudeagent.model.ReportSearchResult;
import claudeagent.rest.DocumentController;
import claudeagent.rest.ReportController;
import lombok.Getter;
import lombok.Setter;


@Service
@Getter @Setter
public class ResearchTools {

	String outputDirectory;

	private final ReportController reportController;

	private final DocumentController documentController;
	
	private final DocumentStoreInterface documentStore;

	public ResearchTools(@Value("${OUTPUT_DIR:./output/}") String outputDirectory, ReportController reportController,
			DocumentController documentController, DocumentStoreInterface documentStore) {
		super();
		this.outputDirectory = outputDirectory;
		this.reportController = reportController;
		this.documentController = documentController;
		this.documentStore = documentStore;
	}
	
	
	@Tool (description="Search the document vector database for a given text query")
	public List<DocumentSearchResult> searchDocumentLibrary(
			@ToolParam (description="This is the query to be used for searching the database for the relevent document chunks")
			String text, 
			@ToolParam (description="This is the maximum number of chunks which should be returned.")
			int limit) {
		List<DocumentSearchResult> searchResults = documentController.queryFile(text, limit);
		return searchResults;
	}
	
	@Tool (description="Find all document chunks for a given document ID")
	public List<DocumentSearchResult> findChunksById(
			@ToolParam (description="This is the document ID begin searched.  The document ID is a unique identifier used by the Document Store "
					              + "to reference the original document.")
			String documentId, 
			@ToolParam (description="This is the maximum number of chunks which should be returned.")
			int limit) {
		List<DocumentSearchResult> searchResults = documentController.findDocumentById(documentId, limit);
		return searchResults;
	}
	
	@Tool (description="Finds a document based on document ID and writes it to disc")
	public void writeDocumentToDisc(
			@ToolParam (description="This is a unique identifier used by the Document Store to reference the original document.")
			String documentId, 
			@ToolParam (description="This is the name of the file to be used for writing the file to disc.")
			String fileName) {
		System.out.println("OUTPUT_DIR is "+outputDirectory);
		byte[] fileData = documentStore.getDocument(documentId);
		System.out.println("Saving "+fileName);
		Path path = Paths.get("./output/"+fileName);
        try {
			Files.write(path, fileData);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Tool (description="Persists an agent report for a completed agent goal.  "
			         + "Call this only once, when the findings have been fully synthesized. "
			         + "This is a record of the work and should stand alone as a complete answer")
	public Long writeAgentReport(
			@ToolParam (description="This is the original goal or question being answered")
			String agentGoal, 
			@ToolParam (description="This is the full report.  It should be a thorough well-synthesized summary of the findings. "
					              + "Write this as if it will be read by someone later who does not have access to the source documents.")
			String reportText, 
	        @ToolParam(description = "The document IDs of every source document that contributed to this report")
			List<String> sourceDocumentIds) {

		Long reportId;
		if (sourceDocumentIds == null) {
			reportId = reportController.writeAgentReport( agentGoal, reportText);
		} else {
			String[] documentIds = new String[sourceDocumentIds.size()];
			for (int i=0; i < sourceDocumentIds.size(); i++) {
				documentIds[i]=sourceDocumentIds.get(i);
			}
			reportId = reportController.writeAgentReport( agentGoal, reportText, documentIds);
		}
		return reportId;
	}
	
	
	
	@Tool (description="Looks up a previously stored agent reports that matches the supplied goal. "
		             + "The table is queried using LIKE with wildcards around the goal, so it's not really a keyword search, but it will "
		             + "pull in everything whose goal contains the supplied goal phrase")
	public List<ReportSearchResult> findAgentReport(
			@ToolParam (description="This is the phrase being searched for in the reports table.  "
					              + "Any record containing this exact phrase will be returned.")
			String agentGoalPhrase, 
			@ToolParam (description="This is the maximum number of report search results which should be returned. "
					              + "A limit of 0 means return all results, but be careful here because it might return too many results.")
			int limit) {
		return reportController.findReports(agentGoalPhrase, limit);
	}

}
