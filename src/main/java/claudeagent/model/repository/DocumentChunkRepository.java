package claudeagent.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import claudeagent.model.DocumentChunk;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long>, JpaSpecificationExecutor<DocumentChunk> {

    @Query(value = "SELECT * FROM document_chunk ORDER BY embedding <=> CAST(:queryVector AS vector) LIMIT :topK", nativeQuery = true)
    public List<DocumentChunk> findNearest(@Param("queryVector") String queryVector,  @Param("topK") int topK);

    @Query(value = "SELECT * FROM document_chunk WHERE document_id=:documentId ORDER BY id ASC LIMIT :limit", nativeQuery = true)
    public List<DocumentChunk> findChunks(@Param("documentId") String queryVector,  @Param("limit") int limit);
    
    @Query(value = "SELECT * FROM document_chunk WHERE document_id=:documentId ORDER BY id ASC", nativeQuery = true)
    public List<DocumentChunk> findChunks(@Param("documentId") String queryVector);
	
}
