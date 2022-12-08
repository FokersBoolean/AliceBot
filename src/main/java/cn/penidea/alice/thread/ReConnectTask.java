package cn.penidea.alice.thread;

import cn.penidea.alice.util.Constants;
import cn.penidea.alice.ws.Client;

/**
 * 功能:重连任务线程
 * 作者:Mr.Fokers
 * 日期：2022年10月08日 14:38
 */

public class ReConnectTask implements Runnable {
    @Override
    public void run() {
        while (true) {
            if (Client.connect(Constants.WS_URL)) {
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
