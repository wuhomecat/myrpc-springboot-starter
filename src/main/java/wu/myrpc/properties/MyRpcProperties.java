package wu.myrpc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="my.rpc")//在application.properties里使用该前缀可以配置这个属性类
public class MyRpcProperties {
    //设置初值，如果配置文件里没有配置，则采用默认值
    private String zkAddresss = "127.0.0.1:2181";
    private Integer serverPort = 19000;
    private String protocol = "myprotocol";

    public String getZkAddresss() {
        return zkAddresss;
    }

    public void setZkAddresss(String zkAddresss) {
        this.zkAddresss = zkAddresss;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
