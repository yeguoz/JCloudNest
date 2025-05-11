package icu.yeguo.cloudnest.config;

import icu.yeguo.cloudnest.model.vo.SettingVO;
import icu.yeguo.cloudnest.service.ISettingService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Resource
    private ISettingService settingService;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        List<SettingVO> mail = settingService.getSettingByType("mail");
        Map<String, String> mailMap = new HashMap<>();
        mail.forEach(setting -> mailMap.put(setting.getName(), setting.getValue()));

        String host = mailMap.get("host");
        int port = Integer.parseInt(mailMap.get("port"));
        String username = mailMap.get("username");
        String password = mailMap.get("password");

        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.debug", "false");

        return mailSender;
    }
}