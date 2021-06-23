package wu.myrpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wu.myrpc.client.discovery.ServiceDiscoverer;
import wu.myrpc.client.net.NetClient;
import wu.myrpc.common.protocol.MessageProtocol;
import wu.myrpc.common.protocol.RequestDatagram;
import wu.myrpc.common.protocol.ResponseDatagram;
import wu.myrpc.common.service.Service;
import wu.myrpc.exception.MyRpcException;
import wu.myrpc.server.register.ZookeeperServiceRegister;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
* 客户端代理工厂：用于创建代理对象 （配置时再做初始化）
* 代理对象：负责根据服务构造和解析报文
* - 创建一个接口的代理对象，根据接口名(服务名)在zookeeper里查找service对象：ZookeeperServiceDiscoverer类
* - 根据Service对象构造请求报文：服务名、方法名\方法参数类型\方法参数（反射而来）；
* - 根据协议将报文编码为字节数组data，连同service对象一起提交给Netty Net Client
* - 获取Netty Net Client返回的响应字节数组，根据协议解码，得到响应报文，返回处理结果。
* */
public class ClientProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegister.class);


    private ServiceDiscoverer serviceDiscoverer;

    private Map<String, MessageProtocol> supportMessageProtocols;

    private NetClient netClient;

    //private Map<Class<?>, Object> objectCache = new HashMap<>();


    public ServiceDiscoverer getServiceDiscoverer() {
        return serviceDiscoverer;
    }

    public void setServiceDiscoverer(ServiceDiscoverer serviceDiscoverer) {
        this.serviceDiscoverer = serviceDiscoverer;
    }

    public Map<String, MessageProtocol> getSupportMessageProtocols() {
        return supportMessageProtocols;
    }

    public void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        this.supportMessageProtocols = supportMessageProtocols;
    }

    public NetClient getNetClient() {
        return netClient;
    }

    public void setNetClient(NetClient netClient) {
        this.netClient = netClient;
    }

    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz 被代理类Class
     * @param <T>   泛型
     * @return 服务代理类
     */
//    @SuppressWarnings("unchecked")
//    public <T> T getProxy(Class<T> clazz) {
//        return (T) this.objectCache.computeIfAbsent(clazz,
//                cls -> newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, new ClientInvocationHandler(cls)));
//    }
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[]{clazz},
                new ClientInvocationHandler(clazz));
    }

    /**
     * 客户端服务代理类invoke函数细节实现
     */
    private class ClientInvocationHandler implements InvocationHandler {
        private Class<?> clazz;

        private Random random = new Random();

        public ClientInvocationHandler(Class<?> clazz) {
            super();
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {

            if (method.getName().equals("toString")) {
                return proxy.getClass().toString();
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }

            // 1、获得服务信息（this表示当前动态代理对象，即接口实现类对象）
            String serviceName = this.clazz.getName();//获取接口名：全限定名

            //通过zookeeper获取接口对应的服务列表
            List<Service> services = serviceDiscoverer.getServices(serviceName);

            if (services == null || services.isEmpty()) {
                throw new MyRpcException("No provider available!");
            }

            // 随机选择一个服务提供者（软负载均衡）
            Service service = services.get(random.nextInt(services.size()));

            // 2、构造request对象
            RequestDatagram req = new RequestDatagram();
            req.setServiceName(service.getName());
            req.setMethodName(method.getName());
            req.setParameterTypes(method.getParameterTypes());
            req.setParameters(args);

            // 3、协议层编组
            // 获得该方法对应的协议
            MessageProtocol protocol = supportMessageProtocols.get(service.getProtocol());
            // 编组请求
            byte[] data = protocol.marshallingRequest(req);

            // 4、调用网络层发送请求：启动客户端
            byte[] repData = netClient.start(data, service);
            //logger.info("收到响应字节：" + repData.length);
            // 5解组响应消息
            ResponseDatagram rsp = protocol.unmarshallingResponse(repData);
            //logger.info("转换成报文：" + rsp.getReturnValue());
            // 6、结果处理
            if (rsp.getException() != null) {
                throw rsp.getException();
            }
            return rsp.getReturnValue();
        }
    }
}
