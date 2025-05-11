package icu.yeguo.cloudnest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChunkDTO {
    private String hash;
    private String filename;
    private Integer totalChunks;
}