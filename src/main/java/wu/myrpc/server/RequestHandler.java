package wu.myrpc.server;

import wu.myrpc.common.protocol.MessageProtocol;
import wu.myrpc.common.protocol.RequestDatagram;
import wu.myrpc.common.protocol.ResponseDatagram;
import wu.myrpc.common.protocol.StatusCode;
import wu.myrpc.server.register.ServiceObject;
import wu.myrpc.server.register.ServiceRegister;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
* 请求处理器：从上层的netty服务器上得到请求字节数组，再将响应字节数组返回给netty服务器
* 基于自定义的通信协议MessageProtocol，提供：
* 1. 请求字节数组解组为请求报文；
* 2. 根据请求报文查找服务实例对象
* 3. 根据反射调用具体的服务方法，得到处理结果
* 4. 将处理结果序列化，响应编组等操作
*
* */
public class RequestHandler {
    private MessageProtocol protocol;
    private ServiceRegister serviceRegister;

    public RequestHandler(MessageProtocol protocol, ServiceRegister serviceRegister) {
        super();
        this.protocol = protocol;
        this.serviceRegister = serviceRegister;
    }

    public byte[] handleRequest(byte[] data) throws Exception {
        // 1、解组消息：反序列化得到请求报文
        RequestDatagram req = this.protocol.unmarshallingRequest(data);

        // 2、查找服务对象
        ServiceObject so = this.serviceRegister.getServiceObject(req.getServiceName());

        ResponseDatagram rsp = null;

        if (so == null) {
            rsp = new ResponseDatagram(StatusCode.NOT_FOUND);
        } else {
            // 3、反射调用对应的过程方法
            try {
                Method m = so.getClazz().getMethod(req.getMethodName(), req.getParameterTypes());
                Object returnValue = m.invoke(so.getObj(), req.getParameters());
                rsp = new ResponseDatagram(StatusCode.SUCCESS);
                rsp.setReturnValue(returnValue);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                rsp = new ResponseDatagram(StatusCode.ERROR);
                rsp.setException(e);
            }
        }

        // 4、编组响应消息：将响应报文做序列化
        return this.protocol.marshallingResponse(rsp);
    }

    public MessageProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    public ServiceRegister getServiceRegister() {
        return serviceRegister;
    }

    public void setServiceRegister(ServiceRegister serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

}
