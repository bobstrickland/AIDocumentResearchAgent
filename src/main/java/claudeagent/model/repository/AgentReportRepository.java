package claudeagent.model.repository;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import claudeagent.model.AgentReport;

public interface AgentReportRepository  extends JpaRepository<AgentReport, Long>, JpaSpecificationExecutor<AgentReport>  {

    @Query(value = "from AgentReport where agentGoal like :agentGoal")
    public List<AgentReport> findReportbyGoal(@Param("agentGoal") String agentGoal);

    @Query(value = "from AgentReport where agentGoal like :agentGoal")
    public List<AgentReport> findReportbyGoal(@Param("agentGoal") String agentGoal, Limit of);
}
