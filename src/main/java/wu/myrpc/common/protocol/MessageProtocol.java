package wu.myrpc.common.protocol;

/*
 * 消息协议接口，即对数据报做编解码的规范，包括：（编解码 = 序列化、反序列化）
 * - 编组请求规范：将请求数据报编码成字节数组
 * - 解组请求规范：将字节数组解码成请求数据报
 * - 编组响应规范：将响应数据报编码成字节数组
 * - 解组响应规范：将字节数组解码成响应数据报
 * */
public interface MessageProtocol {
    /**
     * 编组请求
     * 将请求数据报编码成字节数组，用于传输、存储
     * @param req 请求信息
     * @return 请求字节数组
     * @throws Exception 编组请求异常
     */
    byte[] marshallingRequest(RequestDatagram req) throws Exception;

    /**
     * 解组请求
     * 将字节数组解码成请求数据报，用于内存上处理
     * @param data 请求字节数组
     * @return 请求信息
     * @throws Exception 解组请求异常
     */
    RequestDatagram unmarshallingRequest(byte[] data) throws Exception;

    /**
     * 编组响应
     * 将响应数据报编码成字节数组，用于传输、存储
     * @param rsp 响应信息
     * @return 响应字节数组
     * @throws Exception 编组响应异常
     */
    byte[] marshallingResponse(ResponseDatagram rsp) throws Exception;

    /**
     * 解组响应
     * 将字节数组解码成响应数据报，用于内存上处理
     * @param data 响应字节数组
     * @return 响应信息
     * @throws Exception 解组响应异常
     */
    ResponseDatagram unmarshallingResponse(byte[] data) throws Exception;
}
