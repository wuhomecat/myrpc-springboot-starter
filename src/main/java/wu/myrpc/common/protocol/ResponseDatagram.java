package wu.myrpc.common.protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
* 响应信息封装类：请求数据报（反序列化的结果）
* - 序列化UID：数据报要在网络中传输，需要序列化
* */
public class ResponseDatagram implements Serializable {
    private static final long serialVersionUID = 7160043027513637374L;
    //响应状态码
    private StatusCode status;
    //报文首部字段：用于解决粘包半包问题等
    private Map<String, String> headers = new HashMap<>();
    //返回值
    private Object returnValue;
    //异常
    private Exception exception;

    public ResponseDatagram(StatusCode status) {
        this.status = status;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
