package org.shirakawatyu;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;

public final class Serverob extends JavaPlugin {
    Listener listener = null;
    public static final Serverob INSTANCE = new Serverob();
    private Serverob() {
        super(new JvmPluginDescriptionBuilder("org.shirakawatyu.serverob", "1.0-SNAPSHOT")
                .name("ServerOb")
                .info("一个用于监控服务器是否上线的小插件")
                .author("ShirakawaTyu")
                .build());
    }

    @Override
    public void onEnable() {
        File path = new File("." + File.separator + "config" + File.separator + "org.shirakawatyu.serverob");
        File file = new File("." + File.separator + "config" + File.separator + "org.shirakawatyu.serverob" + File.separator + "conf.properties");
        if(!path.exists()){
            try {
                path.mkdir();
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                fw.write("host=\n" + "port=\n" + "rqq=\n" + "sqq=");
                fw.close();
                getLogger().info("请填写配置文件，然后重启机器人，配置文件位于 ./config/org.shirakawatyu.serverob/conf.properties");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
                listener = GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, event -> {
                new isAvailable(this, file).start();
            });
            getLogger().info("* ServerOb 插件已启动 *");
        }
    }
}

class isAvailable extends Thread{
    private Serverob serverob;
    private File file;
    public isAvailable(Serverob serverob,File file){
        this.serverob = serverob;
        this.file = file;
    }
    @Override
    public void run() {
        serverob.listener.complete();
        Boolean status = true;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        String rqq = properties.getProperty("rqq");
        String sqq = properties.getProperty("sqq");
        Contact contact = Bot.getInstance(Long.parseLong(sqq)).getFriend(Long.parseLong(rqq));
        long time1 = 0;
        while (true) {
            Socket socket = null;
            try {
                time1 = new Date().getTime();
                socket = new Socket(host, Integer.parseInt(port));
                long time2 = new Date().getTime() - time1;
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverob.getLogger().info("连接成功，耗时" + time2 + "ms");
                if(!status){
                    contact.sendMessage("目标：" + host + ":" + port + "已上线，连接耗时：" + time2 + "ms");
                    status = true;
                }
            } catch (IOException e) {
                if(status){
                    serverob.getLogger().info("连接失败");
                    contact.sendMessage("目标：" + host + ":" + port + "已离线");
                    status = false;
                }
            }
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}