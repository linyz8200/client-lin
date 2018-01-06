package com.client.lin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by LinLive on 2018/1/4.
 */
public class ClientHandle extends ChannelInboundHandlerAdapter {

    private Client client;



    public ClientHandle(Client client){
        this.client = client;
    }


    protected void handleMercury(ChannelHandlerContext channelHandlerContext, JSONObject j) {
        System.out.println("client 接收返回数据" + j.toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        client.doConnect();
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        sendPingMsg(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String s = (String)msg;
        System.out.println("代理返回数据" + s);
        JSONObject j = JSON.parseObject(s);
        Integer type = j.getInteger("type");
        if(type == 999){
            //心跳检测
            System.out.println("---------心跳检测连接成功----------");
            return;
        }else{
            //路由服务器
            handleMercury(ctx, j);
        }
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        }
    }


    protected void sendPingMsg(ChannelHandlerContext context) {
        JSONObject j = new JSONObject();
        j.put("type", 0);
        context.channel().writeAndFlush(j.toJSONString());
        System.out.println("心跳检测开始： " + context.channel().remoteAddress());
    }


    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        System.err.println("---READER_IDLE---");
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }

}
