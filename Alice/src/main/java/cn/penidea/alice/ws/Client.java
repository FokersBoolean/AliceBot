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
        if ((groupList.get(groupId) != null && (baseConfigBean.getUserList().get(from) != null || baseConfigBean.isPublic())) || baseConfigBean.getUserList().get("admin").equals(from)) {
            if (msg.contains(baseConfigBean.getAtRobotCQ() + " 问个好")) {
                sendMessage(from, groupId, messageType, "我可是" + baseConfigBean.getRobotName() + "，随随便便就想让我给大家问好？", false);
                return;
            } else if ((MASTER == null || baseConfigBean.getUserList().get("admin").equals(from)) && !baseConfigBean.isConcurrency()) {
                if ((msg.equals(baseConfigBean.getWakeUpWord()) || msg.startsWith(baseConfigBean.getAtRobotCQ()))) {
                    MASTER = from;
                    if (msg.equals(baseConfigBean.getAtRobotCQ()) || msg.equals(baseConfigBean.getAtRobotCQ() + " ") || msg.equals(baseConfigBean.getWakeUpWord())) {
                        sendMessage(from, groupId, messageType, baseConfigBean.getPromptUpWord(), false);
                    }
                }
            }

            if (MASTER != null) {
            	if (from.equals(MASTER) || baseConfigBean.isConcurrency()) {
                if (msg.startsWith(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ() + " ")) {
                    if (msg.equals(baseConfigBean.getAtRobotCQ() + " ")) {
                        msg = msg.substring(msg.indexOf("]") + 3);
                    } else {
                        msg = msg.substring(msg.indexOf("]") + 2);
                    }
                    isAt = true;
                }
                if (msg.equals(baseConfigBean.getStandbyWord())) {
                    MASTER = null;
                    sendMessage(from, groupId, messageType, baseConfigBean.getStandbyPrompt(), false);
                    return;
                }
                if (baseConfigBean.getUserList().get("admin").equals(from)) {
                    if (msg.startsWith("也和这位聊吧[CQ:at,qq=")) {
                    	if (!baseConfigBean.isPublic()) {
                        String qq = msg.substring(msg.indexOf("也和这位聊吧[CQ:at,qq=") + 16, msg.indexOf("]"));
                        baseConfigBean.getUserList().put(qq, "");
                        sendMessage(from, groupId, messageType, "要让我和[CQ:at,qq=" + qq + "] 聊啊，行吧！", false);
                    	} else {
                    		sendMessage(from, groupId, messageType, "我现在已经可以和大家都聊天了啊", false);
                    	}
                        if (!isAlone) {
                        MASTER = null;
                        }
                        return;
                    } else if (msg.startsWith("別理[CQ:at,qq=")) {
                    	if (!baseConfigBean.isPublic()) {
                        String qq = msg.substring(msg.indexOf("别理[CQ:at,qq=") + 12, msg.indexOf("]"));
                        if (baseConfigBean.getUserList().get(qq) != null) {
                            baseConfigBean.getUserList().remove(qq);
                            sendMessage(from, groupId, messageType, "好吧，那我再也不理[CQ:at,qq=" + qq + "] 了", false);
                        } else {
                            sendMessage(from, groupId, messageType, "哼哼，我早就不理他了", false);
                        }
                    	} else {
                    		sendMessage(from, groupId, messageType, "你不是让我和大家都聊天吗？", false);
                    	}
                        if (!isAlone) {
                        MASTER = null;
                        }
                        return;
                    } else if (msg.equals("#设为公有")) {
                        threadPoolMember = new ConcurrentHashMap<>();
                        baseConfigBean.setPublic(true);
                        sendMessage(from, groupId, messageType, "想让我和大家都聊天？好吧...", false);
                        if (!isAlone) {
                        MASTER = null;
                        }
                        return;
                    } else if (msg.equals("#设为私有")) {
                        threadPoolMember = new ConcurrentHashMap<>();
                        baseConfigBean.setPublic(false);
                        sendMessage(from, groupId, messageType, "哼哼，我现在只和我认识的人聊天了", false);
                        if (!isAlone) {
                        MASTER = null;
                        }
                        return;
                    } else if (msg.equals("#重置所有会话")) {
                        if (baseConfigBean.isConcurrency() && isAlone) {
                            threadMap = ExpiringMap.builder().
                                    expiration(1000 * 60 * 20, TimeUnit.MILLISECONDS).
                                    expirationPolicy(ExpirationPolicy.CREATED).
                                    build();
                            threadPoolMember = new ConcurrentHashMap<>();
                            sendMessage(from, groupId, messageType, "啊啊啊，我聊天的记忆，全部消失了...", false);
                            return;
                        }
                        chatGPTService.reset();
                        sendMessage(from, groupId, messageType, "啊啊啊，我和你聊天的记忆，正在消失...", false);
                        if (!isAlone) {
                            MASTER = null;
                            }
                        return;
                    } else if (msg.equals("#重置我的会话") && baseConfigBean.isConcurrency()) {
                        threadMap.remove(from);
                        sendMessage(from, groupId, messageType, "啊啊啊，我和你聊天的记忆，消失了...", false);
                        return;
                    } else if (msg.equals("#开启多线程")) {
                    	if (!isAlone) {
                            MASTER = null;
                            }
                        baseConfigBean.setConcurrency(true);
                        sendMessage(from, groupId, messageType, "哈？要让我同时和每个人聊天？也不是不行", false);
                        return;
                    } else if (msg.equals("#开启单线程")) {
                        baseConfigBean.setConcurrency(false);
                        threadPoolMember = new ConcurrentHashMap<>();
                        threadMap = ExpiringMap.builder().
                                expiration(1000 * 60 * 20, TimeUnit.MILLISECONDS).
                                expirationPolicy(ExpirationPolicy.CREATED).
                                build();
                        sendMessage(from, groupId, messageType, "同时和那么多人聊天即使对于我" + baseConfigBean.getRobotName() + "来说还是有点困难啊", false);
                        return;
                    } else if (msg.equals("#开启一问到底")) {
                        MASTER = from;
                        isAlone = true;
                        threadPoolMember = new ConcurrentHashMap<>();
                        sendMessage(from, groupId, messageType, "真懒，连每次@我都懒得@", false);
                        return;
                    } else if (msg.equals("#在本群开机")) {
                        groupList.put(groupId, "");
                        sendMessage(from, groupId, messageType, "哼哈哈，本王来了！", false);
                        return;
                    } else if (msg.equals("#在本群关机")) {
                        groupList.remove(groupId);
                        sendMessage(from, groupId, messageType, "走了走了", false);
                        return;
                    } else if (msg.equals("#开关自添加")) {
                    	if (!baseConfigBean.isPublic()) {
                        if (baseConfigBean.isAddself()) {
        					baseConfigBean.setAddself(false);
        					sendMessage(from, groupId, messageType, "哼哼，接下来就算你求我，我也不会愿意和你讲话了", false);
        				} else {
        					baseConfigBean.setAddself(true);
        					sendMessage(from, groupId, messageType, "想和我聊天？@我+“也和我聊聊呗”，我会考虑考虑的，哈哈", false);
        				}
                    	} else {
                    		sendMessage(from, groupId, messageType, "我现在愿意和大家都聊天，没必要开关这个", false);
                    	}
                        if (!isAlone) {
                        MASTER = null;
                        }
                        return;
                    } else if (msg.equals("#开关等待词")) {
        				if (baseConfigBean.isLoading()) {
        					baseConfigBean.setLoading(false);
        					sendMessage(from, groupId, messageType, "哈？不想让我说 " + baseConfigBean.getLoadingWord() + " 了？唉，行吧", false);
        				} else {
        					baseConfigBean.setLoading(true);
        					sendMessage(from, groupId, messageType, "嘿嘿，那你每次问我问题我都要说 " + baseConfigBean.getLoadingWord(), false);
        				}
        				return;
        			} else if (msg.equals("#帮助")) {
                        String commands = "也和这位聊吧@xxx 让我愿意和他聊天\n----------\n" +
                                "别理@xxx 让我不理他\n----------\n" +
                                "#设为公有 让我愿意和群里所有人聊天\n----------\n" +
                                "#设为私有 让我只想和愿意的人聊天\n----------\n" +
                                "#重置所有会话 别想清除我的所有聊天记忆\n----------\n" +
                                "#重置我的会话 别想清除我和你的聊天记忆\n----------\n" +
                                "#开启多线程 让我同时和许多人聊天\n----------\n" +
                                "#开启单线程 让我一次只和一个人聊天\n----------\n" +
                                "#开启一问到底 不想叫本王又想和我聊天，真懒\n----------\n" +
                                "#在本群开机 让我愿意在这个群聊天\n----------\n" +
                                "#在本群关机 让我不愿意在这个群聊天\n----------\n" +
                                "#开关自添加 让我接受或不接受别人把他自己添加到我的聊天对象列表中\n----------\n" +
                                "#开关等待词 不想听“" + baseConfigBean.getLoadingWord() + "”了是吧";
                        sendMessage(from, groupId, messageType, commands, false);
                    }
                }

                if (!msg.equals(baseConfigBean.getAtRobotCQ()) && !msg.equals(baseConfigBean.getAtRobotCQ() + " ") && !msg.equals(baseConfigBean.getWakeUpWord())) {
                    if (baseConfigBean.isConcurrency() && isAt && threadPoolMember.get(from) == null && !isAlone) {
                        if (baseConfigBean.isLoading()) {
                    	sendMessage(from, groupId, messageType, baseConfigBean.getLoadingWord(), false);
                        }
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
                            if (baseConfigBean.isLoading()) {
                            sendMessage(from, groupId, messageType, baseConfigBean.getLoadingWord(), false);
                            }
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
            } else {
        		if ((msg.equals(baseConfigBean.getWakeUpWord()) || msg.startsWith(baseConfigBean.getAtRobotCQ()))) {
            		sendMessage(from, groupId, messageType, "别吵吵，我还在和[CQ:at,qq=" + MASTER + "] 聊天呢，等下哈", false);
            	}
        		return;
              }
        } else if (groupList.get(groupId) != null && !baseConfigBean.isPublic() && msg.contains(baseConfigBean.getAtRobotCQ() + " 也和我聊聊呗")){
        	if (baseConfigBean.isAddself()) {
        	baseConfigBean.getUserList().put(from, "");
        	if (from.equals(MASTER)) {
            sendMessage(from, groupId, messageType, "行行行，我也可以和你聊天", false);
        	} else {
        		sendMessage(from, groupId, messageType, "行，等我和[CQ:at,qq=" + MASTER + "] 聊完再跟你聊天", false);
        	}
            return;
        	} else {
        		sendMessage(from, groupId, messageType, "就你还想和本大王聊天？", false);
        		return;
        	}
        }
    }

    public static String getRobotName() {
        return baseConfigBean.getRobotName();
    }
}
