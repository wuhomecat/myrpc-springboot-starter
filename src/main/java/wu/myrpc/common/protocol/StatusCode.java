package wu.myrpc.common.protocol;
/*
* 响应状态码类
* */
public enum StatusCode {
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "ERROR"),
    NOT_FOUND(404, "NOT FOUND");

    //枚举对象的结构
    private int code;
    private String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
