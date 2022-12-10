package cn.penidea.alice.service;

import cn.penidea.alice.entity.Message;

import java.util.Map;

/**
 * 功能:chatGPT服务接口
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 8:51
 */

public interface ChatGPTService {

    //提问接口
    String askQuestion(String question);

    //多线程提问接口
    void askQuestionInThread(Message message, String question, String conversationId, String parentId, String from, String group, String msgType);

    //刷新对话
    boolean reset();

    //刷新会话认证
    boolean refresh();

}
