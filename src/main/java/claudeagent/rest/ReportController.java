package claudeagent.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import claudeagent.model.ReportSearchResult;
import claudeagent.service.ReportService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/report")
@Getter @Setter @AllArgsConstructor @Builder
public class ReportController {
	private static final Logger log = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private final ReportService reportService;

	@GetMapping("/find")
	public List<ReportSearchResult> findReports(
			@RequestParam (name="goal") String agentGoal, 
			@RequestParam (name="limit", defaultValue="0") int limit) {
		return reportService.findReports(agentGoal, limit);
	}
	

	@PostMapping("/write")
	public Long writeAgentReport(
			@RequestParam (name="agentGoal") String agentGoal, 
			@RequestParam (name="reportText") String reportText, 
			@RequestParam (name="documentIds") String... sourceDocumentIds) {
		return reportService.writeAgentReport(agentGoal, reportText, sourceDocumentIds);
	}
	
	
	
	
	

}
