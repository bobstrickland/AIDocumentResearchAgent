package claudeagent.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import claudeagent.agent.ResearchAgent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@RestController
@RequestMapping("/agent")
@Getter @Setter @AllArgsConstructor @Builder
public class AgentController {

	private final ResearchAgent agent;
	
	@PostMapping("/run")
	public String runAgent(@RequestBody String goal) {
		return agent.run(goal);
	}

}
