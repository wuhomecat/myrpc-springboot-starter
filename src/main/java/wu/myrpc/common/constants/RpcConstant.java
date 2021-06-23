package wu.myrpc.common.constants;

/*
* 通用常量
* */
public class RpcConstant {
    //构造器设为私有：避免被外部修改
    private RpcConstant(){}
    /*
    * zookeeper服务注册地址
    * */
    public static final String ZK_SERVICE_PATH = "/myrpc";

    /*
    * 编码
    * */
    public static final String UTF_8 = "UTF-8";

    /*
    * 路径分隔符
    * */
    public static final String PATH_DELIMITER = "/";
}
