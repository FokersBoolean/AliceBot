package cn.penidea.alice.config;

import cn.penidea.alice.chatgpt.Chatbot;
import com.alibaba.fastjson2.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能:chatGPT配置
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 8:42
 */

@Configuration
public class ChatGPTConfig {

    @Value("${chatGPT.email}")
    private String email;
    @Value("${chatGPT.password}")
    private String password;
    @Value("${chatGPT.sessionToken}")
    private String sessionToken;

    @Bean
    public Chatbot chatBot() {
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("session_token", sessionToken);
        return new Chatbot(params, null);
    }

}
