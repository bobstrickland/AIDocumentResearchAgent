package claudeagent.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import claudeagent.model.ConversationHistory;
import claudeagent.model.DocumentChunk;

public interface ConversationHistoryRepository extends JpaRepository<ConversationHistory, Long>, JpaSpecificationExecutor<ConversationHistory>  {


    @Query(value = "SELECT * FROM conversation_history WHERE session_id=:sessionId ORDER BY embedding <=> CAST(:queryVector AS vector) LIMIT :topK", nativeQuery = true)
    public List<ConversationHistory> findNearest(@Param("queryVector") String queryVector,  @Param("sessionId") String sessionId, @Param("topK") int topK);
    
    @Query(value="SELECT * FROM conversation_history WHERE session_id=:sessionId ORDER BY id desc LIMIT :topK", nativeQuery = true)
    public List<ConversationHistory> findMostRecent(@Param("sessionId") String sessionId, @Param("topK") int topK);
    
}
