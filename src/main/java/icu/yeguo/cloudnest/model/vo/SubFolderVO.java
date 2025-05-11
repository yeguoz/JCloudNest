package icu.yeguo.cloudnest.model.vo;

import icu.yeguo.cloudnest.model.dto.FolderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class SubFolderVO {
    private Long currentId;
    private Long parentId;
    private List<FolderDTO> list;
}