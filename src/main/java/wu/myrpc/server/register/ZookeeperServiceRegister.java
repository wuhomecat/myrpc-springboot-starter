package wu.myrpc.server.register;

import com.alibaba.fastjson.JSON;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wu.myrpc.common.service.Service;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import static wu.myrpc.common.constants.RpcConstant.*;

//便于直接使用设置好的常量

/*
 * zookeeper服务注册器：以zookeeper作为注册中心，负责：
 * 1. 在本地map中绑定：继承LocalServiceRegister
 * 2. 在zookeeper中注册：服务提供方充当zookeeper的客户端
 * - key父节点 = zookeeper服务器ip:port + /service/ + 服务名
 * - value子节点 = key + / + service对象的json字符串
 * service对象里存放了服务提供方的ip,服务名,和它通信的协议
 * 要区分提供服务的服务器（运行当前类的机器）和zookeeper服务器
 * */
public class ZookeeperServiceRegister extends LocalServiceRegister implements ServiceRegister {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegister.class);

    //服务提供方端口
    protected Integer port;
    //服务提供方IP地址
    protected String host;
    //要和服务提供方通信必须采用的通信协议
    protected String protocol;

    //zookeeper客户端
    CuratorFramework zkClient;
    //zookeeper客户端重连策略:间隔3s重试一次，最多重试10次
    RetryPolicy retry = new ExponentialBackoffRetry(3000, 10);

    /*
     * 构造函数
     * @param zkAddress zookeeper服务器ip:port
     * */
    public ZookeeperServiceRegister(String zkAddress, Integer port, String protocol){
        try {
            this.host = InetAddress.getLocalHost().getHostAddress();//当前服务器IP地址
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
        this.protocol = protocol;
        this.zkClient = CuratorFrameworkFactory.newClient(zkAddress, retry);
        this.zkClient.start();
    }

    /*
     * 服务注册方法：
     * 1. 在本地map中绑定服务名和服务实例对象（使用父类的register方法）
     * 2. 再调用服务暴露方法，将服务信息对象注册到zookeeper上
     * - 区分服务实例对象和服务信息对象
     * */
    @Override
    public void register(ServiceObject so) {
        //1.在本地map中绑定
        super.register(so);
        //2.在zookeeper中注册
        Service service = new Service();
        service.setAddress(host + ":" + port);
        service.setName(so.getClazz().getName());//服务名存放的是服务接口名，支持多态
        service.setProtocol(protocol);
        this.exportService(service);

    }

    /*
     * 服务暴露方法：在zookeeper中注册服务信息对象，服务提供方充当zookeeper的客户端
     * - key父节点 = zookeeper服务器ip:port + /service/ + 服务名
     * - value子节点 = key + / + service对象的json字符串
     * */
    private void exportService(Service service) {
        String serviceName = service.getName();
        //将service对象序列化
        String serviceValue = JSON.toJSONString(service);
        logger.info("service to json" + serviceValue);

        try {
            //对json字符串做URL编码：将=,:等特殊字符转换成URL编码，避免出现歧义
            serviceValue = URLEncoder.encode(serviceValue, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //key = myrpc/service/服务名
        String key = ZK_SERVICE_PATH + PATH_DELIMITER + "service" + PATH_DELIMITER + serviceName;
        try {
            //如果当前节点不存在，则创建该节点（如有必要还需创建父节点，创建的是持久化节点）
            if(zkClient.checkExists().forPath(key) == null){
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //val = myrpc/service/服务名/服务信息json
        String val = key + PATH_DELIMITER + serviceValue;
        try {
            //如果指定节点已存在，则先将其删除，相当于只保留最新的服务提供方
            if(zkClient.checkExists().forPath(val) != null){
                zkClient.delete().forPath(val);
            }
            //创建服务提供方节点，创建的是临时节点，服务器断开则清除
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(val);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
