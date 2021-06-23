package wu.myrpc.client.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wu.myrpc.client.net.handler.ClientHandler;
import wu.myrpc.common.service.Service;

/*
 * netty客户端，提供netty网络通信能力，包括：
 * 1. 根据注册中心获得的"提供服务的服务器地址"建立netty连接
 * 2. 向服务器端发送请求字节数组：data
 * 3. 从服务器端读取响应字节数组：start()的返回值
 * */
public class NettyNetClient implements NetClient {
    private static Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    /**
     * 启动netty客户端，完成：
     * 1. 根据service对象提取服务器端地址,端口
     * 2. 配置netty客户端，与指定服务器端地址建立连接
     * 3. ClientHandler负责：发送请求字节数组，接收响应字节数组
     *
     * @param data    请求数据，由代理对象传递而来
     * @param service 服务信息，从zookeeper上获取而来
     * @return 响应数据
     */
    @Override
    public byte[] start(byte[] data, Service service){
        //1. 根据service对象提取服务器端地址,端口
        String[] addInfoArray = service.getAddress().split(":");
        String serverAddress = addInfoArray[0];
        String serverPort = addInfoArray[1];

        ClientHandler clientHandler = new ClientHandler(data);
        //SendHandler sendHandler = new SendHandler(data);
        byte[] respData = null;
        //2. 配置netty客户端，与指定服务器端地址建立连接
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            //3. ClientHandler负责：发送请求字节数组，接收响应字节数组
                            p.addLast(clientHandler);
                        }
                    });

            // 启动客户端连接
            b.connect(serverAddress, Integer.parseInt(serverPort)).sync();
            //从handler中获取响应字节数组
            respData = (byte[]) clientHandler.rspData();
            logger.info("SendRequest get reply: {}", respData);
        } catch (InterruptedException e) {
            logger.info("netty连接失败");
            e.printStackTrace();
        } finally {
            // 释放线程组资源
            group.shutdownGracefully();
        }

        return respData;
    }
}
