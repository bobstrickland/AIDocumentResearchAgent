package claudeagent.configuration;

import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolExecutionConfig {

    @Bean
    ToolExecutionExceptionProcessor toolExecutionExceptionProcessor() {
        return DefaultToolExecutionExceptionProcessor.builder()
                .alwaysThrow(true)
                .build();
    }
}
