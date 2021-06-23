package wu.myrpc.exception;
/*
* 自定义异常类
* */
public class MyRpcException extends RuntimeException {
    public MyRpcException(String message) {
        super(message);
    }
}
