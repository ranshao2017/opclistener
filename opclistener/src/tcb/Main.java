package tcb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

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
        /*List<String> itemIdList = new ArrayList<String>();
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
        }*/

        // 4.获取配置的所有监测点
        final Map<String, String> tagMap = getConfigMap();
        if(null == tagMap){
            System.out.println("未配置监测点");
            return;
        }else{
            System.out.println("配置监测点数：" + tagMap.size());
        }

        // 5.注册回调
        opcClient.subscribe(new Observer() {
            public void update(Observable observable, Object arg) {
                Result result = (Result) arg;
                String deviceName = result.getItemId().substring(0, result.getItemId().lastIndexOf("."));

                Jedis jedis = new Jedis("localhost", 6379, 60 * 1000);
                System.out.println("连接redis成功");
                String deviceObj = jedis.get(deviceName);
                if(null == deviceObj){
                    JSONObject obj = new JSONObject();
                    obj.put(tagMap.get(result.getItemId()), result.getItemState().getValue().toString());
                    jedis.set(deviceName, obj.toJSONString());
                }else{
                    JSONObject obj = JSON.parseObject(deviceObj);
                    obj.put(tagMap.get(result.getItemId()), result.getItemState().getValue().toString());
                    jedis.set(deviceName, obj.toJSONString());
                }

                System.out.println("update result=" + result.getItemState().getValue().toString());
            }
        });

        // 6.添加监听检测点的数据
        // client和server在不同网段，可以访问
        for(String key : tagMap.keySet()){
            opcClient.syncReadObject(key, 1000);
        }

        /**
         * TODO 问题
         * client和server在不同网段，访问失败，比如：server为10.1.1.132，该网段下面又连接了扩展路由器，192.168.1.x，client为192.168.1.100
         */
//        opcClient.asyncReadObject("Channel1.TEST.VW1066", 500);

        // 延迟
//        delay(5 * 60 * 1000);

        System.out.println("请输入：EXIT 退出");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String inStr = scanner.next().toString();
            if("EXIT".equals(inStr.toUpperCase())){
                System.out.print("成功退出！");
                System.exit(0);
            }
        }
    }

    private static Map<String, String> getConfigMap() {
        try{
            InputStream is = Main.class.getClassLoader().getResourceAsStream("opcconfig.txt");
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String confStr = result.toString("UTF-8");
            if(null != confStr && !"".equals(confStr)){
                String[] tagArr = confStr.split(",");
                Map<String, String> tagMap = new HashMap<String, String>();
                for (int i = 0; i < tagArr.length; i ++){
                    tagMap.put(tagArr[i].split("-")[0], tagArr[i].split("-")[1]);
                }
                return tagMap;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
