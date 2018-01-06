package com.client.lin;

import com.alibaba.fastjson.JSONObject;

import java.util.Scanner;
import java.util.UUID;

/**
 * Created by LinLive on 2018/1/4.
 */
public class ClientMain {

    public static void main(String arg[]) throws Exception {
        Client client = new Client();
        String name = null;
        while (true){
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入字符串:");
            name = sc.nextLine();  //读取字符串型输入
            JSONObject j = new JSONObject();
            j.put("type", 1);
            j.put("requestId", UUID.randomUUID().toString());
            j.put("data", name);
            client.sendData(j);
            Thread.sleep(2000);
        }
    }
}
