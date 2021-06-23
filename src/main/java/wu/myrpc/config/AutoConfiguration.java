package wu.myrpc.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wu.myrpc.client.ClientProxyFactory;
import wu.myrpc.client.discovery.ZookeeperServiceDiscoverer;
import wu.myrpc.client.net.NettyNetClient;
import wu.myrpc.common.protocol.MessageProtocol;
import wu.myrpc.common.protocol.MessageProtocolSerialize;
import wu.myrpc.properties.MyRpcProperties;
import wu.myrpc.server.NettyRpcServer;
import wu.myrpc.server.RequestHandler;
import wu.myrpc.server.RpcServer;
import wu.myrpc.server.register.DefaultRpcProcessor;
import wu.myrpc.server.register.ServiceRegister;
import wu.myrpc.server.register.ZookeeperServiceRegister;

import java.util.HashMap;
import java.util.Map;

/*
* springboot自动配置类：用于构建starter
* 对使用到的类做初始化，生成对应的bean对象
* */
@Configuration
@EnableConfigurationProperties(wu.myrpc.properties.MyRpcProperties.class)//加载hello对应的配置信息,并将HelloProperties注册到IOC容器
public class AutoConfiguration {

    @Autowired
    MyRpcProperties myRpcProperties;

    @Bean
    public DefaultRpcProcessor defaultRpcProcessor(){
        return new DefaultRpcProcessor();
    }

    /*
    * 初始化ClientProxyFactory
    * 对ClientProxyFactory类里的成员变量做初始化
    * */
    @Bean
    public ClientProxyFactory clientProxyFactory(){
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory();
        //设置服务发现者
        clientProxyFactory.setServiceDiscoverer(new ZookeeperServiceDiscoverer(myRpcProperties.getZkAddresss()));
        //设置支持的通信协议
        Map<String , MessageProtocol> supportMessageProtocols = new HashMap<>();
        supportMessageProtocols.put(myRpcProperties.getProtocol(), new MessageProtocolSerialize());
        clientProxyFactory.setSupportMessageProtocols(supportMessageProtocols);
        //设置网络层实现
        clientProxyFactory.setNetClient(new NettyNetClient());
        return clientProxyFactory;
    }
    /*
    * 初始化服务注册器
    * */
    @Bean
    public ServiceRegister serviceRegister() {
        return new ZookeeperServiceRegister(
                myRpcProperties.getZkAddresss(),
                myRpcProperties.getServerPort(),
                myRpcProperties.getProtocol());
    }

    /*
    * 初始化RequestHandler
    * */
    @Bean
    public RequestHandler requestHandler(@Autowired ServiceRegister serviceRegister) {
        return new RequestHandler(new MessageProtocolSerialize(), serviceRegister);
    }

    /*
    * 初始化RPCServer
    * */
    @Bean
    public RpcServer rpcServer(@Autowired RequestHandler requestHandler) {
        return new NettyRpcServer(myRpcProperties.getServerPort(),
                myRpcProperties.getProtocol(), requestHandler);
    }
}
