package cn.penidea.alice.ws;

import cn.penidea.alice.entity.Message;
import cn.penidea.alice.entity.Params;
import cn.penidea.alice.entity.Request;
import cn.penidea.alice.service.impl.ChatGPTServiceImpl;
import cn.penidea.alice.thread.ReConnectTask;
import cn.penidea.alice.util.BaseConfigBean;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
    public static ChatGPTServiceImpl chatGPTService;
    private static boolean connecting = false;
    private static String MASTER = null;
    private static boolean isAlone = false;
    private static BaseConfigBean baseConfigBean;
    private static ConcurrentHashMap<String, String> groupList = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> threadPoolMember = new ConcurrentHashMap<>();
    public static ExpiringMap<String, Map<String, Object>> threadMap = ExpiringMap.builder().
            expiration(1000 * 60 * 20, TimeUnit.MILLISECONDS).
            expirationPolicy(ExpirationPolicy.CREATED).
            build();


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
        boolean isAt = false;
        if ((baseConfigBean.getUserList().get(from) != null || baseConfigBean.isPublic() || groupList.get(groupId) != null) && (baseConfigBean.getUserList().get("admin").equals(from) || !isAlone)) {
            if (msg.contains(baseConfigBean.getAtRobotCQ() + " Say hello")) {
                sendMessage(from, groupId, messageType, "大家好!我是" + baseConfigBean.getRobotName(), false);
                return;
            } else if ((MASTER == null || baseConfigBean.getUserList().get("admin").equals(from)) && !baseConfigBean.isConcurrency()) {
                if ((msg.equals(baseConfigBean.getWakeUpWord()) || msg.startsWith(baseConfigBean.getAtRobotCQ()))) {
                    MASTER = from;
                    if (msg.equals(baseConfigBean.getAtRobotCQ()) || msg.equals(baseConfigBean.getAtRobotCQ() + " ") || msg.equals(baseConfigBean.getWakeUpWord())) {
                        sendMessage(from, groupId, messageType, baseConfigBean.getPromptUpWord(), false);
                    }
                } else if (msg.equals(baseConfigBean.getStandbyWord())) {
                    MASTER = null;
                    sendMessage(from, groupId, messageType, baseConfigBean.getStandbyPrompt(), false);
                }
            }

            if ((MASTER != null && from.equals(MASTER)) || baseConfigBean.isConcurrency()) {
                if (msg.startsWith(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ() + " ")) {
                    if (msg.equals(baseConfigBean.getAtRobotCQ() + " ")) {
                        msg = msg.substring(msg.indexOf("]") + 3);
                    } else {
                        msg = msg.substring(msg.indexOf("]") + 2);
                    }
                    isAt = true;
                }
                if (baseConfigBean.getUserList().get("admin").equals(from)) {
                    if (msg.startsWith("add [CQ:at,qq=") && !baseConfigBean.isPublic()) {
                        String qq = msg.substring(msg.indexOf("add [CQ:at,qq=") + 14, msg.indexOf("]"));
                        baseConfigBean.getUserList().put(qq, "");
                        sendMessage(from, groupId, messageType, "添加主人" + qq + "成功!", false);
                        MASTER = null;
                        return;
                    } else if (msg.startsWith("del [CQ:at,qq=") && !baseConfigBean.isPublic()) {
                        String qq = msg.substring(msg.indexOf("del [CQ:at,qq=") + 14, msg.indexOf("]"));
                        if (baseConfigBean.getUserList().get(qq) != null) {
                            baseConfigBean.getUserList().remove(qq);
                            sendMessage(from, groupId, messageType, "移除主人" + qq + "成功!", false);
                        } else {
                            sendMessage(from, groupId, messageType, "移除失败，主人列表里并没有他", false);
                        }
                        MASTER = null;
                        return;
                    } else if (msg.equals("#public")) {
                        isAlone = false;
                        threadPoolMember = new ConcurrentHashMap<>();
                        baseConfigBean.setPublic(true);
                        sendMessage(from, groupId, messageType, "机器人已设为公有化", false);
                        MASTER = null;
                        return;
                    } else if (msg.equals("#private")) {
                        isAlone = false;
                        threadPoolMember = new ConcurrentHashMap<>();
                        baseConfigBean.setPublic(false);
                        sendMessage(from, groupId, messageType, "机器人已设为私有化", false);
                        MASTER = null;
                        return;
                    } else if (msg.equals("#reset")) {
                        if (baseConfigBean.isConcurrency() && isAlone) {
                            threadMap = ExpiringMap.builder().
                                    expiration(1000 * 60 * 20, TimeUnit.MILLISECONDS).
                                    expirationPolicy(ExpirationPolicy.CREATED).
                                    build();
                            threadPoolMember = new ConcurrentHashMap<>();
                            sendMessage(from, groupId, messageType, "所有会话已重置", false);
                            return;
                        }
                        chatGPTService.reset();
                        sendMessage(from, groupId, messageType, "会话已重置", false);
                        MASTER = null;
                        return;
                    } else if (msg.equals("#reset me") && baseConfigBean.isConcurrency()) {
                        threadMap.remove(from);
                        sendMessage(from, groupId, messageType, "会话已重置", false);
                        return;
                    } else if (msg.equals("#more")) {
                        MASTER = null;
                        baseConfigBean.setConcurrency(true);
                        sendMessage(from, groupId, messageType, "已开启多线程", false);
                        return;
                    } else if (msg.equals("#less")) {
                        baseConfigBean.setConcurrency(false);
                        threadPoolMember = new ConcurrentHashMap<>();
                        threadMap = ExpiringMap.builder().
                                expiration(1000 * 60 * 20, TimeUnit.MILLISECONDS).
                                expirationPolicy(ExpirationPolicy.CREATED).
                                build();
                        sendMessage(from, groupId, messageType, "已切换单人问答", false);
                        return;
                    } else if (msg.equals("#alone")) {
                        MASTER = from;
                        isAlone = true;
                        threadPoolMember = new ConcurrentHashMap<>();
                        sendMessage(from, groupId, messageType, "一问到底模式已开启", false);
                        return;
                    } else if (msg.equals("#add this")) {
                        groupList.put(groupId, "");
                        sendMessage(from, groupId, messageType, "已将本群纳入权限列表", false);
                        return;
                    } else if (msg.equals("#del this")) {
                        groupList.remove(groupId);
                        sendMessage(from, groupId, messageType, "已将本群从权限列表中移除", false);
                        return;
                    } else if (msg.equals("#help")) {
                        String commands = "add @xxx 给某个qq添加权限\n----------\n" +
                                "del @xxx 删除某个qq的权限\n----------\n" +
                                "#public 将设置为所有人可用\n----------\n" +
                                "#private 将设为权限列表内可用\n----------\n" +
                                "#reset 在多线程模式下清除所有会话，单线程模式下清除会话\n----------\n" +
                                "#reset me 用于多线程时清除管理员自己的会话\n----------\n" +
                                "#more 开启多线程模式\n----------\n" +
                                "#less 关闭多线程模式\n----------\n" +
                                "#alone 开启一问到底(既无需唤醒机器人)\n----------\n" +
                                "#add this 将当前的群聊加入权限列表\n----------\n" +
                                "#del this 将当前的群聊从权限列表中移除";
                        sendMessage(from, groupId, messageType, commands, false);
                    }
                }

                if (!msg.equals(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ() + " ") && !msg.equals(baseConfigBean.getWakeUpWord())) {
                    if (baseConfigBean.isConcurrency() && isAt && threadPoolMember.get(from) == null && !isAlone) {
                        sendMessage(from, groupId, messageType, baseConfigBean.getLoadingWord(), false);
                        String conversationId = null;
                        String parentId = UUID.randomUUID().toString();
                        Map<String, Object> map = threadMap.get(from);
                        if (map != null) {
                            conversationId = map.get("conversation_id").toString();
                            parentId = map.get("parent_id").toString();
                            threadPoolMember.put(from, 1);
                        }
                        chatGPTService.askQuestionInThread(message, msg, conversationId, parentId, from, groupId, messageType);
                        return;
                    }
                    try {
                        if (MASTER != null && threadPoolMember.get(from) == null) {
                            threadPoolMember.put(from, 1);
                            sendMessage(from, groupId, messageType, baseConfigBean.getLoadingWord(), false);
                            String answer = chatGPTService.askQuestion(msg);
                            answer = answer.replace("Assistant", baseConfigBean.getRobotName());
                            sendMessage(from, groupId, messageType, answer, false);
                            if (!isAlone) {
                                MASTER = null;
                            }
                        }
                    } catch (Exception e) {
                        chatGPTService.refresh();
                    }
                }

            }
        }
    }

    public static String getRobotName() {
        return baseConfigBean.getRobotName();
    }
}
