package cn.penidea.alice.service;

/**
 * 功能:chatGPT服务接口
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 8:51
 */

public interface ChatGPTService {

    //提问接口
    String askQuestion(String question);

    //刷新对话
    boolean reset();

    //刷新会话认证
    boolean refresh();
}
