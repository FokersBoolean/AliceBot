package cn.penidea.alice.service.impl;

import cn.penidea.alice.chatgpt.Chatbot;
import cn.penidea.alice.entity.Message;
import cn.penidea.alice.service.ChatGPTService;
import cn.penidea.alice.thread.ChatSessionThread;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 功能:ChatGPT服务接口实现类
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 8:52
 */

@Service
public class ChatGPTServiceImpl implements ChatGPTService {

    @Autowired
    private Chatbot chatbot;

    @Autowired
    private ExecutorService executorService;

    @Override
    public String askQuestion(String question) {
        try {
            Map<String, Object> message = chatbot.getChatResponse(question, "stream");
            // 按换行符拆分消息
            return message.get("message").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Token失效或服务器延迟，如多次尝试询问无果请尝试重新获取cookie";
        }
    }

    @Override
    public void askQuestionInThread(Message message, String question, String conversationId, String parentId, String from, String group, String msgType) {
        executorService.submit(new ChatSessionThread(message, conversationId, parentId, question, chatbot, from, group, msgType));

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
