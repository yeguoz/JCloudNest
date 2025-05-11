package icu.yeguo.cloudnest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MergedFileInfo {
    private String sourceName;
    private String hash;
    private String first256KBHash;
}