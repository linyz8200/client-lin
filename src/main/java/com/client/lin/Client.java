package com.client.lin;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by LinLive on 2018/1/4.
 */
public class Client {

    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private Bootstrap bootstrap;

    public Client(){
        init();
    }


    private void init(){
        System.err.println("-----------客户端启动开始-----------");
        try {
            bootstrap = new Bootstrap();
            bootstrap
                    .group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            p.addLast(new IdleStateHandler(0, 0, 5));
                            p.addLast(new MsgPackDecode());
                            p.addLast(new MsgPackEncode());
                            p.addLast(new ClientHandle(Client.this));
                        }
                    });
            doConnect();
            System.err.println("-----------客户端启动结束-----------");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 重连机制,每隔2s重新连接一次服务器
     */
    protected void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        ChannelFuture future = bootstrap.connect("127.0.0.1", 12345);

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    channel = futureListener.channel();
                    System.out.println("连接其他服务端成功");
                } else {
                    System.out.println("连接其他服务端失败，开始重试，间隔两秒");

                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 10, TimeUnit.SECONDS);
                }
            }
        });
    }




    /**
     * 发送数据
     * @throws Exception
     */
    public void sendData(JSONObject j) throws Exception {
        if(channel != null && channel.isActive()){
            System.out.println("客户端 开始转发数据" + j);
            channel.writeAndFlush(j.toJSONString());
            return;
        }
        System.out.println("客户端 channel 已经失效，不发生数据");
        throw new RuntimeException("客户端 channel 已经失效，不发生数据");
}
}
