package cn.penidea.alice.thread;

import cn.penidea.alice.util.BaseConfigBean;
import cn.penidea.alice.ws.Client;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 功能:重连任务线程
 * 作者:Mr.Fokers
 * 日期：2022年10月08日 14:38
 */

public class ReConnectTask implements Runnable {
    private static BaseConfigBean baseConfigBean;
    @Autowired
    public void setBaseConfigBean(BaseConfigBean baseConfigBean) {
        ReConnectTask.baseConfigBean = baseConfigBean;
    }
    @Override
    public void run() {
        while (true) {
            if (Client.connect(baseConfigBean.getCqHttpWs())) {
                System.out.println("重连成功");
                break;
            } else {
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void execute() {
        new Thread(new ReConnectTask()).start();
    }
}
