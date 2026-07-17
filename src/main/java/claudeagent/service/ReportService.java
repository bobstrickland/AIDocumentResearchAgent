package claudeagent.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import claudeagent.model.AgentReport;
import claudeagent.model.ReportSearchResult;
import claudeagent.model.repository.AgentReportRepository;
import claudeagent.rest.ReportController;

@Service
public class ReportService {
	
	@Autowired
	private AgentReportRepository reportRepository;

	private static final Logger log = LoggerFactory.getLogger(ReportController.class);

	public List<ReportSearchResult> findReports(String agentGoal, int limit) {
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
	

	public Long writeAgentReport(String agentGoal, String reportText, String... sourceDocumentIds) {
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
