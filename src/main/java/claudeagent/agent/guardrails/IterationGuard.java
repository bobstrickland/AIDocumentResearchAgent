package claudeagent.agent.guardrails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IterationGuard {
	
    private final int MAX_TOTAL_CALLS;
    private final int MAX_ERROR_CALLS;
    private final long AGENT_TIMEOUT_SECONDS;
    

    private final Map<String, AtomicInteger> totalCallCounts = new ConcurrentHashMap<String, AtomicInteger>();
    private final Map<String, AtomicInteger> errorCallCounts = new ConcurrentHashMap<String, AtomicInteger>();
    private final Map<String, Instant>       runtimeMap      = new ConcurrentHashMap<String, Instant>();
    
    
    
    private static final Logger log = LoggerFactory.getLogger(IterationGuard.class);
    public IterationGuard(@Value("${agent.guard.max-tool-calls:15}") int MAX_TOTAL_CALLS, 
                          @Value("${agent.guard.max-error-calls:3}")  int MAX_ERROR_CALLS,
                          @Value("${agent.guard.timeout.seconds:30}")  long AGENT_TIMEOUT_SECONDS) {
		super();
		this.MAX_TOTAL_CALLS = MAX_TOTAL_CALLS;
		this.MAX_ERROR_CALLS = MAX_ERROR_CALLS;
		this.AGENT_TIMEOUT_SECONDS = AGENT_TIMEOUT_SECONDS;
	}
    
    public void startAgent(String sessionId) {
    	runtimeMap.put(sessionId, Instant.now());
    }

    public void recordCall(String sessionId) throws AgentIterationLimitExceededException, AgentTimeLimitExceededException {
        int total = totalCallCounts.computeIfAbsent(sessionId, k -> new AtomicInteger(0)).incrementAndGet();
        if (total > MAX_TOTAL_CALLS) {
            throw new AgentIterationLimitExceededException(false, MAX_TOTAL_CALLS);
        }
        synchronized (this) {
        	if (runtimeMap.containsKey(sessionId)) {
        		Instant starttingTime = runtimeMap.get(sessionId);
        		if (starttingTime.plusSeconds(AGENT_TIMEOUT_SECONDS).isBefore(Instant.now())) {
        			throw new AgentTimeLimitExceededException();
        		}
        	} else {
            	runtimeMap.put(sessionId, Instant.now());
        	}
        }
    }

    public void recordError(String sessionId) throws AgentIterationLimitExceededException {
    	recordError(sessionId, null);
    }

    public void recordError(String sessionId, Exception e) throws AgentIterationLimitExceededException {
        int errors = errorCallCounts.computeIfAbsent(sessionId, k -> new AtomicInteger(0)).incrementAndGet();
        int total = totalCallCounts.computeIfAbsent(sessionId, k -> new AtomicInteger(0)).incrementAndGet();
		log.debug("Error for "+sessionId + " Errors ["+errors+"] Total calls ["+total+"]");

        if (errors > MAX_ERROR_CALLS) {
            throw new AgentIterationLimitExceededException(true, MAX_ERROR_CALLS );
        }
        if (total > MAX_TOTAL_CALLS) {
            throw new AgentIterationLimitExceededException(false, MAX_TOTAL_CALLS);
        }
    }

    public void reset(String sessionId) {
		log.debug("Resetting "+sessionId);
        totalCallCounts.remove(sessionId);
        errorCallCounts.remove(sessionId);
        runtimeMap.remove(sessionId);
    }
}
