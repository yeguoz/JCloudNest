package icu.yeguo.cloudnest.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChunkVO {
    private String uploadId;
    private String fingerprint;
    private String fileName;
    private Integer chunkIndex;
    private Long chunkSize;
    private Integer totalChunks;
    private Integer uploadedChunks;
    private boolean complete;
}