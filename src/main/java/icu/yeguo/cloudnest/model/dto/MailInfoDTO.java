package icu.yeguo.cloudnest.model.dto;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class MailInfoDTO {
    private SettingItem host;
    private SettingItem port;
    private SettingItem username;
    private SettingItem password;
    private SettingItem personal;
    private SettingItem registerSubject;
    private SettingItem registerTemplate;
    private SettingItem forgetSubject;
    private SettingItem forgetTemplate;

    public List<SettingItem> getAllSettings() {
        return Arrays.asList(
                host, port, username, password, personal,
                registerSubject, registerTemplate,
                forgetSubject, forgetTemplate
        );
    }
}