package icu.yeguo.cloudnest.model.vo;


import icu.yeguo.cloudnest.model.dto.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@AllArgsConstructor
@Data
public class FileVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6035523316824552630L;
    private List<FileDTO> list;
}
