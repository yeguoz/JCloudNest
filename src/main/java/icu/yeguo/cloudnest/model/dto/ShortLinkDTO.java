package icu.yeguo.cloudnest.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "ShortLinkDTO", description = "分享链接数据",
        requiredProperties = {"userId", "sourceId", "userFileId", "passwordEnabled", "password", "isDir", "downloadCount",
                "previewEnabled", "expireTimeEnabled", "expireTime"})
@Data
public class ShortLinkDTO {
    @Schema(description = "userId")
    private Integer userId;
    @Schema(description = "文件或目录id")
    private Long sourceId;
    @Schema(description = "用户文件id")
    private Long userFileId;
    @Schema(description = "启用密码")
    private Boolean passwordEnabled;
    @Schema(description = "访问密码")
    private String password;
    @Schema(description = "是否是目录")
    private Integer isDir;
    @Schema(description = "可下载次数")
    private Integer remainingDownloads;
    @Schema(description = "是否开启预览")
    private Boolean previewEnabled;
    @Schema(description = "启用过期时间")
    private Boolean expireTimeEnabled;
    @Schema(description = "过期时间")
    private String expireTime;
}
