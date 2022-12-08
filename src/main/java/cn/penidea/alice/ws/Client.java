package cn.penidea.alice.ws;

import cn.penidea.alice.entity.Friend;
import cn.penidea.alice.entity.Message;
import cn.penidea.alice.entity.Params;
import cn.penidea.alice.entity.Request;
import cn.penidea.alice.service.impl.ChatGPTServiceImpl;
import cn.penidea.alice.thread.ReConnectTask;
import cn.penidea.alice.util.BaseConfigBean;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

/**
 * 功能:监听类
 * 作者:Mr.Fokers
 * 日期：2022年09月28日 9:37
 */

@ClientEndpoint
@Component
@Slf4j
public class Client {

    private Session session;
    private static Client INSTANCE;
    private static ChatGPTServiceImpl chatGPTService;
    private static boolean connecting = false;
    private static String MASTER = null;
    private static BaseConfigBean baseConfigBean;


    @Autowired
    public void setChatGPTService(ChatGPTServiceImpl chatGPTService) {
        Client.chatGPTService = chatGPTService;
    }

    @Autowired
    public void setBaseConfigBean(BaseConfigBean baseConfigBean) {
        Client.baseConfigBean = baseConfigBean;
    }


    private Client() {
    }

    private Client(String url) throws DeploymentException, IOException {
        session = ContainerProvider.getWebSocketContainer().connectToServer(this, URI.create(url));
    }

    public synchronized static boolean connect(String url) {
        try {
            INSTANCE = new Client(url);
            connecting = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("连接失败");
            return false;
        }
    }

    public synchronized static void reConnect() {
        if (!connecting) {
            connecting = true;
            if (INSTANCE != null) {
                INSTANCE.session = null;
                INSTANCE = null;
            }
        }
        ReConnectTask.execute();
    }

    @OnOpen
    public void onOpen(Session session) {
        log.info("消息监听开启成功");
    }

    @OnMessage
    public void onMessage(String json) {
        Message message = JSONObject.parseObject(json, Message.class);
        String msg = message.getMessage();
        String postType = message.getPost_type();
        if (postType != null && postType.equals("message")) {
            try {
                filterFunction(message);
            } catch (IOException e) {
                sendMessage(baseConfigBean.getUserList().get("admin"), null, "private", "Alice Error in" + new Date(), false);
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("服务已关闭");
        reConnect();
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("服务异常断开");
        throwable.printStackTrace();
        reConnect();
    }

    public static void sendMessage(String json) {
        Client.INSTANCE.session.getAsyncRemote().sendText(json);
    }

    public static void sendMessage(String qq, String groupId, String type, String msg, boolean escape) {
        Request<Params> request = new Request<>();
        request.setAction("send_msg");
        Params params = new Params();
        params.setUser_id(qq);
        params.setGroup_id(groupId);
        params.setMessage(msg);
        params.setAuto_escape(escape);
        params.setMessage_type(type);
        request.setParams(params);
        sendMessage(JSON.toJSONString(request));
    }


    public static void filterFunction(Message message) throws IOException {
        String messageType = message.getMessage_type(); //消息类型
        String msg = message.getMessage(); //消息内容
        String from = message.getUser_id();//发送方qq
        String groupId = message.getGroup_id();//群聊号
        if (baseConfigBean.getUserList().get(from) != null) {
            if (msg.contains(baseConfigBean.getAtRobotCQ() + " Say hello")) {
                sendMessage(from, groupId, messageType, "Hello everyone,I m Alice!", false);
                return;
            } else if (MASTER == null || baseConfigBean.getUserList().get("admin").equals(from)) {
                if ((msg.equals(baseConfigBean.getWakeUpWord()) || msg.startsWith(baseConfigBean.getAtRobotCQ()))) {
                    MASTER = from;
                    if (msg.equals(baseConfigBean.getAtRobotCQ()) || msg.equals(baseConfigBean.getWakeUpWord())) {
                        sendMessage(from, groupId, messageType, baseConfigBean.getPromptUpWord(), false);
                    }
                } else if (msg.equals(baseConfigBean.getStandbyWord())) {
                    MASTER = null;
                    sendMessage(from, groupId, messageType, "Your welcome!", false);
                }
            }

            if (MASTER != null && from.equals(MASTER)) {
                if (msg.startsWith(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ())) {
                    msg = msg.substring(msg.indexOf("]") + 2);
                }
                if (baseConfigBean.getUserList().get("admin").equals(from)) {
                    if (msg.startsWith("add [CQ:at,qq=")) {
                        String qq = msg.substring(msg.indexOf("add [CQ:at,qq=") + 14, msg.indexOf("]"));
                        baseConfigBean.getUserList().put(qq, "");
                        sendMessage(from, groupId, messageType, "添加主人" + qq + "成功!", false);
                        MASTER = null;
                        return;
                    } else if (msg.startsWith("del [CQ:at,qq=")) {
                        String qq = msg.substring(msg.indexOf("del [CQ:at,qq=") + 14, msg.indexOf("]"));
                        if (baseConfigBean.getUserList().get(qq) != null) {
                            baseConfigBean.getUserList().remove(qq);
                            sendMessage(from, groupId, messageType, "移除主人" + qq + "成功!", false);
                        } else {
                            sendMessage(from, groupId, messageType, "移除失败，主人列表里并没有他", false);
                        }
                        MASTER = null;
                        return;
                    } else if (msg.equals("#reset chat")) {
                        chatGPTService.reset();
                        sendMessage(from, groupId, messageType, "会话已重置", false);
                        MASTER = null;
                        return;
                    }
                }
                try {
                    if (!msg.equals(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getWakeUpWord())) {
                        sendMessage(from, groupId, messageType, "正在检索...", false);
                        String answer = chatGPTService.askQuestion(msg);
                        answer = answer.replace("Assistant", baseConfigBean.getRobotName());
                        sendMessage(from, groupId, messageType, answer, false);
                        MASTER = null;
                    }
                } catch (Exception e) {
                    chatGPTService.refresh();
                }
            }
        }
    }
}
