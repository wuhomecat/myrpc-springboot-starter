package wu.myrpc.client.discovery;


import wu.myrpc.common.service.Service;

import java.util.List;

/*
* 服务发现者抽象类，定义服务发现规范
* */
public interface ServiceDiscoverer {
    List<Service> getServices(String name);
}
