package wu.myrpc.client.net.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/*
* 客户端handler，ChannelInboundHandler，负责：
* 1. 请求报文的发送：连接建立成功便发送，channelActive
* 2. 响应报文的接收与解码：read事件发生才接收，channelRead
*     提供一个获取响应结果的方法rspData()供外部调用，使用countdownLatch来同步channelRead，
* */
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private CountDownLatch countDown;
    private Object readMsg = null;//读取到的响应字节数组
    private byte[] data;//待发送的请求字节数组

    public ClientHandler(byte[] data) {
        countDown = new CountDownLatch(1);
        this.data = data;
    }

    /**
     * 当连接服务端成功后，发送请求数据
     *
     * @param ctx 通道上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Successful connection to server：{}", ctx);
        ByteBuf reqBuf = Unpooled.buffer(data.length);
        reqBuf.writeBytes(data);
        logger.info("Client sends message：{}", reqBuf);
        ctx.writeAndFlush(reqBuf);
    }

    /**
     * 当读事件发生，也就是读取到响应数据时触发：
     *     读取数据，数据读取完毕释放CD锁
     *
     * @param ctx 上下文
     * @param msg ByteBuf，读取到的数据就存放在msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("Client reads message: {}", msg);
        ByteBuf msgBuf = (ByteBuf) msg;
        //将读取到的数据转存到readMsg上，对外部公开
        byte[] resp = new byte[msgBuf.readableBytes()];
        msgBuf.readBytes(resp);
        readMsg = resp;
        countDown.countDown();
    }

    /**
     * 等待读取数据完成，才能获取到响应数据（响应字节数组）
     *
     * @return 响应数据
     * @throws InterruptedException 异常
     */
    public Object rspData() throws InterruptedException {
        countDown.await();
        return readMsg;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        logger.error("Exception occurred：{}", cause.getMessage());
        ctx.close();
    }
}
