package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class AuthInfoDTO {
    private SettingItem registerEnabled;
    private SettingItem registerGroup;
    private SettingItem registerCaptcha;
    private SettingItem loginCaptcha;

    public List<SettingItem> getAllSettings() {
        return Arrays.asList(registerEnabled, registerGroup, registerCaptcha, loginCaptcha);
    }
}