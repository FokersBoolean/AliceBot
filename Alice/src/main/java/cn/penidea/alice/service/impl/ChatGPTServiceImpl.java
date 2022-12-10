package cn.penidea.alice.service.impl;

import cn.penidea.alice.chatgpt.Chatbot;
import cn.penidea.alice.service.ChatGPTService;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能:ChatGPT服务接口实现类
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 8:52
 */

@Service
public class ChatGPTServiceImpl implements ChatGPTService {

    @Autowired
    private Chatbot chatbot;

    @Override
    public String askQuestion(String question) {
        try {
            List<String> formattedParts = new ArrayList<>();
            Map<String, Object> message = chatbot.getChatResponse(question, "stream");
            // 按换行符拆分消息
            return message.get("message").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong!";
        }
    }

    @Override
    public boolean reset() {
        try {
            chatbot.resetChat();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean refresh() {
        try {
            chatbot.refreshSession();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
