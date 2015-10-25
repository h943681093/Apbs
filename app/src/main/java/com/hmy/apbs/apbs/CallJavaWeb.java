package com.hmy.apbs.apbs;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
public class CallJavaWeb extends Thread{
    Handler handler=null;
    private String urlDate="http://219.231.176.87:8080/PublicBicycleSys/getTaskservlet?";
    private String CarTaskID = null;
    private HttpURLConnection conn;
    private URL url;
    private InputStream is;
    public CallJavaWeb(Handler handler){
        this.handler=handler;
    }
    public void doStart(String CarTaskID)
    {
        this.CarTaskID=CarTaskID;
        this.start();
    }
    @Override
    public void run() // 线程处理的内容
    {
        try {
            //封装访问服务器的地址
            urlDate += "CarTaskID="+CarTaskID+"";
            url=new URL(urlDate);
            try {
                conn=(HttpURLConnection) url.openConnection();
                conn.connect();
                is=conn.getInputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(is));
                String line=null;
                StringBuffer sb=new StringBuffer();
                while((line=br.readLine())!=null){
                    sb.append(line);
                }
                System.out.println(sb.toString());
                Message msg = new Message();
                Bundle bundle = new Bundle();
                // 将查询的结果放进msg中
                bundle.putString("result", sb.toString());
                msg.setData(bundle);
                msg.what = 0;
                handler.sendMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        super.run();
    }
}
