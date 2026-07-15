package claudeagent.model;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Id;
//import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import claudeagent.model.converters.VectorConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "document_chunk", schema = "public")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = VectorConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(768)", nullable = false)
    private float[] embedding;

    @Column(name = "source_title", length = 512)
    private String sourceTitle;

    @Column(name = "source_url", length = 1024)
    private String sourceUrl;

    @Column
    private List<String> tags;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

	
}
