package icu.yeguo.cloudnest.model.vo;


import icu.yeguo.cloudnest.model.dto.FileFolderItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class FileFolderVO {
    private List<FileFolderItem> list;
}
