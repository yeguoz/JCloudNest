package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UploadRequest {
    private List<String> uploadIds;
}