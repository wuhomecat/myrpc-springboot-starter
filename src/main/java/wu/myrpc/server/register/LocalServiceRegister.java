package wu.myrpc.server.register;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地服务注册器：服务的本地绑定
 * 将服务名和服务实例对象存入本地map，就可以按服务名获取服务实例对象
 */
public class LocalServiceRegister implements ServiceRegister {

    //本地map作为注册中心，key=服务名，value=服务类实例对象
    private Map<String, ServiceObject> serviceMap = new HashMap<>();


    @Override
    public void register(ServiceObject so){
        if (so == null) {
            throw new IllegalArgumentException("Parameter cannot be empty.");
        }

        this.serviceMap.put(so.getName(), so);
    }

    @Override
    public ServiceObject getServiceObject(String name) {
        return this.serviceMap.get(name);
    }
}
