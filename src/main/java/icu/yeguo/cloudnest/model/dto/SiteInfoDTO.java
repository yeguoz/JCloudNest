package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SiteInfoDTO {
    private SettingItem url;

    public List<SettingItem> getAllSettings() {
        return Collections.singletonList(url);
    }
}