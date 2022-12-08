package cn.penidea.alice.config;

import cn.penidea.alice.util.BaseConfigBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 功能:存储用户
 * 作者:Mr.Fokers
 * 日期：2022年12月09日 0:30
 */

@Configuration
public class ClientBaseConfig {
    @Value("${clientBaseConfig.admin}")
    private String adminQQ;
    @Value("${clientBaseConfig.robot}")
    private String robotQQ;
    @Value("${clientBaseConfig.wakeUpWord}")
    private String wakeUpWord;
    @Value("${clientBaseConfig.robotName}")
    public String robotName;
    @Value("${clientBaseConfig.standbyWord}")
    private String standbyWord;
    @Value("${clientBaseConfig.promptUpWord}")
    private String promptUpWord;

    @Bean
    public BaseConfigBean baseConfig() {
        HashMap<String, String> userList = new HashMap<>();
        userList.put(adminQQ, "");
        userList.put("admin", adminQQ);
        return new BaseConfigBean(userList, "[CQ:at,qq=" + robotQQ + "]", wakeUpWord, robotName, standbyWord, promptUpWord);
    }
}
