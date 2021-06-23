package wu.myrpc.common.protocol;

import wu.myrpc.common.constants.RpcConstant.*;
import wu.myrpc.common.serializer.Serializer;

/*
* 消息协议接口的实现：选择具体的序列化/反序列化算法做编解码
* 可通过SERIALIZER选用：jdk自带算法、fastjson
* */
public class MessageProtocolSerialize implements MessageProtocol{

    //指定具体的序列化算法
    public static final Serializer SERIALIZER = Serializer.JAVA;
    /*
    * 请求数据报的编码 = 序列化
    * */
    @Override
    public byte[] marshallingRequest(RequestDatagram req) throws Exception {
        return SERIALIZER.serialize(req);
    }

    /*
    * 请求数据报的解码 = 反序列化
    * */
    @Override
    public RequestDatagram unmarshallingRequest(byte[] data) throws Exception {
        return SERIALIZER.deserialize(RequestDatagram.class, data);
    }

    /*
     * 响应数据报的编码 = 序列化
     * */
    @Override
    public byte[] marshallingResponse(ResponseDatagram rsp) throws Exception {
        return SERIALIZER.serialize(rsp);
    }

    @Override
    public ResponseDatagram unmarshallingResponse(byte[] data) throws Exception {
        return SERIALIZER.deserialize(ResponseDatagram.class, data);
    }
}
