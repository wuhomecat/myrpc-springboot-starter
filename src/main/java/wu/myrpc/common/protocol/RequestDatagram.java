package wu.myrpc.common.protocol;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
* 请求信息封装类：请求数据报（反序列化的结果）
* - 序列化UID：数据报要在网络中传输，需要序列化
* */
public class RequestDatagram implements Serializable {

    private static final long serialVersionUID = -7941549281328897401L;
    //服务名
    private String serviceName;
    //方法名
    private String methodName;
    //首部信息：用于解决粘包半包问题等
    private Map<String, String> headers = new HashMap<>();
    //参数类型
    private Class<?>[] parameterTypes;
    //参数对象
    private Object[] parameters;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
