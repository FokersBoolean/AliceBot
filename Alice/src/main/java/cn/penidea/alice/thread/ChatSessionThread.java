package cn.penidea.alice.thread;

import cn.penidea.alice.chatgpt.Chatbot;
import cn.penidea.alice.entity.Message;
import cn.penidea.alice.ws.Client;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能:会话线程
 * 作者:Mr.FoLio
 * 日期：2022年12月10日 23:20
 */

@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionThread implements Runnable {
    private Message message;
    private String conversationId;
    private String parentId;
    private String question;
    private Chatbot chatbot;
    private String qq;
    private String group;
    private String msgType;

    @Override
    public void run() {
        try {
            Map<String, Object> params = chatbot.getChatResponseByThread(question, conversationId, parentId, "stream");
            // 按换行符拆分消息
            Client.threadMap.put(qq, params);
            String answer = params.get("message").toString();
            answer = answer.replace("Assistant", Client.getRobotName());
            Client.sendMessage(qq, group, msgType, "[CQ:reply,id=" + message.getMessage_id() + "]" + answer, false);
            Client.threadPoolMember.remove(qq);
        } catch (Exception e) {
            Client.threadPoolMember.remove(qq);
            e.printStackTrace();
        }
    }
}
