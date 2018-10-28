package tcb;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Main {
    public static void main(String[] args) {
        String host = "localhost";// server
        String domain = "localhost";// domain
        String progId = "Kepware.KEPServerEX.V5";
        String user = "OPCUser";// server上的访问用户
        String password = "Opc123456";// 访问用户的密码

        OpcClient opcClient = new OpcClient();
        // 1.显示server上的opc server应用列表
        opcClient.showAllOPCServer(host, user, password, domain);

        // 2.连接指定的opc server
        boolean ret = opcClient.connectServer(host, progId, user, password, domain);
        if (!ret) {
            System.out.println("connect opc server fail");
            return;
        }

        // 3.检查opc server上的检测点
        List<String> itemIdList = new ArrayList<String>();
        itemIdList.add("Channel1.TEST.C1");
        itemIdList.add("Channel1.TEST.C14");
        itemIdList.add("Channel1.TEST.C2");
        itemIdList.add("Channel1.TEST.C3");

        itemIdList.add("Channel1.TEST.I1_3");
        itemIdList.add("Channel1.TEST.I4_4");
        itemIdList.add("Channel1.TEST.I4_5");
        itemIdList.add("Channel1.TEST.I4_6");

        itemIdList.add("Channel1.TEST.I5_4");
        itemIdList.add("Channel1.TEST.VD2600");
        itemIdList.add("Channel1.TEST.VD2604");
        itemIdList.add("Channel1.TEST.VW1016");

        itemIdList.add("Channel1.TEST.VW1066");
        itemIdList.add("Channel1.TEST.VW390");
        ret = opcClient.checkItemList(itemIdList);
        if (!ret) {
            System.out.println("not contain item list");
            return;
        }

        // 4.注册回调
        opcClient.subscribe(new Observer() {
            public void update(Observable observable, Object arg) {
                Result result = (Result) arg;

                System.out.println("update result=" + result.getItemState().getValue().toString());
            }
        });

        // 5.添加监听检测点的数据
        // client和server在不同网段，可以访问
        opcClient.syncReadObject("Channel1.TEST.VD2600", 500);
        /**
         * TODO 问题
         * client和server在不同网段，访问失败，比如：server为10.1.1.132，该网段下面又连接了扩展路由器，192.168.1.x，client为192.168.1.100
         */
//        opcClient.asyncReadObject("Channel1.TEST.VW1066", 500);

        // 延迟
        delay(5 * 60 * 1000);
    }

    private static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
