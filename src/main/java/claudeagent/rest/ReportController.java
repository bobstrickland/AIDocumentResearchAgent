package claudeagent.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import claudeagent.model.AgentReport;
import claudeagent.model.ReportSearchResult;
import claudeagent.model.repository.AgentReportRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/report")
@Getter @Setter @AllArgsConstructor @Builder
public class ReportController {

	@Autowired
	private final AgentReportRepository reportRepository;

	@GetMapping("/find")
	public List<ReportSearchResult> findReports(
			@RequestParam (name="goal") String agentGoal, 
			@RequestParam (name="limit", defaultValue="0") int limit) {
		if (agentGoal == null || agentGoal.isBlank()) {
			return null;
		} else {
			List<AgentReport> agentReportList;
			if (limit > 0) {
				agentReportList= reportRepository.findReportbyGoal("%"+agentGoal.trim()+"%");
			} else {
				agentReportList= reportRepository.findReportbyGoal("%"+agentGoal.trim()+"%", Limit.of(limit));
			}
			if (agentReportList != null && !agentReportList.isEmpty()) {
				List<ReportSearchResult> resultList = agentReportList.stream()
				.map(report -> new ReportSearchResult(report.getAgentGoal(), report.getReportText(), report.getSourceDocumentIds(), report.getCreatedAt()))
				.toList();
				return resultList;
			} else {
				return null;
			}
		}
	}
	

	@PostMapping("/write")
	public Long writeAgentReport(
			@RequestParam (name="agentGoal") String agentGoal, 
			@RequestParam (name="reportText") String reportText, 
			@RequestParam (name="documentIds") String... sourceDocumentIds) {
		Long reportId = null;
		AgentReport report = new AgentReport();
		report.setAgentGoal(agentGoal);
		report.setReportText(reportText);
		if (sourceDocumentIds != null && sourceDocumentIds.length > 0) {
			List<String> documentIdList = new ArrayList<String>(Arrays.asList(sourceDocumentIds));
			report.setSourceDocumentIds(documentIdList);
		}
		reportRepository.save(report);
		reportRepository.flush();
		reportId = report.getId();
		return reportId;
	}
	
	
	
	
	

}
