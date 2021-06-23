package wu.myrpc.server.register;
/*
* 服务注册器接口，定义服务注册的规范
* */
public interface ServiceRegister {

    void register(ServiceObject so);

    ServiceObject getServiceObject(String name);
}
