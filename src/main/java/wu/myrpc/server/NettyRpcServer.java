package wu.myrpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * netty服务器端，提供netty网络通信能力
 * 包括：服务开启、服务关闭
 * */
public class NettyRpcServer extends RpcServer {
    private static Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private Channel channel;

    public NettyRpcServer(int port, String protocol, RequestHandler handler) {
        super(port, protocol, handler);
    }

    /*
     * 启动服务器：
     * 1. 配置netty服务：
     *  - 设置全连接队列长度SO_BACKLOG
     *  - 向pipeline注册ChannelRequestHandler
     * 2. 启动后就无限loop，处理客户端连接请求和IO请求
     */
    @Override
    public void start() {

        //1个主reactor负责处理accept事件
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //默认数量的从reactor负责处理IO事件
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)//设置全连接队列长度
                    .childHandler(
                            new ChannelInitializer<SocketChannel>() {//接收到的客户端channel
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ChannelPipeline pipeline = ch.pipeline();
                                    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                                    //读取请求数据，调用服务方法，返回响应数据
                                    pipeline.addLast(new ChannelRequestHandler());
                                }
                            }

                    );
            //启动服务
            ChannelFuture cf = bootstrap.bind(port).sync();
            logger.info("Server started successfully");
            //对客户端channel赋值
            channel = cf.channel();
            //等待服务通道关闭
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放线程组资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /*
    * 关闭服务器
    * 关闭连接的channel，所有连接的channel都关闭服务器就会自动关闭
    * */
    @Override
    public void stop() {
        this.channel.close();
    }

    /*
    * 创建服务器执行的handler：
    * 1. 连接事件：连接建立成功输出日志
    * 2. read事件：读取客户端发送的数据到字节数组，提交给处理器，获得处理结果字节数组，响应给客户端
    * 3. 读取完事件：刷新ctx给下一个handler
    * */
    private class ChannelRequestHandler extends ChannelInboundHandlerAdapter{
        //连接事件：连接建立成功输出日志
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("Channel active: {}", ctx);
        }

        //read事件
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            logger.info("The Server receives a message: {}", msg);
            //读取到的请求数据转存到字节数组上
            ByteBuf msgBuf = (ByteBuf) msg;
            byte[] req = new byte[msgBuf.readableBytes()];
            msgBuf.readBytes(req);
            //字节数组传递给上一层的处理器去处理，得到返回的响应字节数组
            byte[] rsp = handler.handleRequest(req);
            //响应字节数组发送给客户端
            ByteBuf rspBuf = Unpooled.buffer(rsp.length);
            rspBuf.writeBytes(rsp);
            ctx.write(rspBuf);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }
    }

}
