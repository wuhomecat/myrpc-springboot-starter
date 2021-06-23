package wu.myrpc.client.net;

import wu.myrpc.common.service.Service;

/**
 * 网络请求客户端，定义网络请求规范
 *  - 客户端发送请求报文的方法
 */
public interface NetClient {
    //启动客户端：发送请求数据给服务器端
    byte[] start(byte[] data, Service service);
}
