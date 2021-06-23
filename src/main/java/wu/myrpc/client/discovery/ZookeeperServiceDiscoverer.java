package wu.myrpc.client.discovery;

import com.alibaba.fastjson.JSON;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wu.myrpc.common.service.Service;
import wu.myrpc.server.register.ZookeeperServiceRegister;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static wu.myrpc.common.constants.RpcConstant.*;

//便于使用声明好的常量

/*
 * zookeeper服务发现器：以zookeeper作为注册中心，负责：
 * 1. 根据服务接口名，在zookeeper中查找对应的节点：getChildren
 * 2. getChildren查找指定节点的所有子节点，子节点表示该接口的所有实现的服务信息对象，
 *    返回的是List<String>
 *
 * */
public class ZookeeperServiceDiscoverer implements ServiceDiscoverer {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegister.class);

    //引入一个本地map，存放查找过的<服务名，Service对象列表>
    private Map<String, List<Service>> registerMap;

    private CuratorFramework zkClient;
    //zookeeper客户端重连策略:间隔3s重试一次，最多重试10次
    RetryPolicy retry = new ExponentialBackoffRetry(3000, 10);

    /*
     * 构造函数：创建zookeeper客户端并和服务器端连接
     * */
    public ZookeeperServiceDiscoverer(String zkAddress){
        super();
        //zookeeper客户端连接zookeeper服务器端
        zkClient = CuratorFrameworkFactory.newClient(zkAddress, retry);
        zkClient.start();
        this.registerMap = new HashMap<>();
    }

    /*
     * 获取服务方法，使用zookeeper客户端，根据服务名获取服务列表
     * @return 返回服务列表
     * */
    @Override
    public List<Service> getServices(String name) {
        //key=/myrpc/service/服务名
        String key = ZK_SERVICE_PATH + PATH_DELIMITER + "service" + PATH_DELIMITER + name;

        /*
        * 为服务发现增加watcher机制：当zookeeper上的服务发生修改时，就会重新获取一次key的所有子节点
        * */
        //为指定节点创建监听对象
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, key, true);
        //绑定监听器
        /*
        * 当事件发生时，直接重新获取一次key节点的所有子节点，写入一个新的service列表
        * */
        pathChildrenCache.getListenable().addListener((curatorFramework, event) -> {
            System.out.println("子节点变化了...更新map");
            //开辟新的Service对象列表存放更新后的service对象列表
            List<Service> newServices = new ArrayList<>();

            //直接重新获取key的所有子节点
            List<String> children = zkClient.getChildren().forPath(key);
            //解析每个字符串元素：URL解码->json转Service对象
            for(String child : children){
                String deChild = URLDecoder.decode(child, StandardCharsets.UTF_8);
                Service service = JSON.parseObject(deChild, Service.class);
                newServices.add(service);
            }
            registerMap.put(key, newServices);
        });
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            logger.info("监听器启动失败");
        }

        //根据key获取对应的service对列表
        if(registerMap.containsKey(key)){
            return registerMap.get(key);
        }
        else{
            List<Service> services = new ArrayList<>();
            try {
                //获取key下所有子节点，返回的是List<String>
                List<String> children = zkClient.getChildren().forPath(key);
                //解析每个字符串元素：URL解码->json转Service对象
                for (String child : children) {
                    String deChild = URLDecoder.decode(child, StandardCharsets.UTF_8);
                    Service service = JSON.parseObject(deChild, Service.class);
                    services.add(service);
                }
                registerMap.put(key, services);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return registerMap.get(key);
        }
    }
}