package wu.myrpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/*
* 被该注解标记的服务类，会在spring启动时自动注册到本地服务器和zookeeper上，可供远程RPC访问，在服务器端使用
* value = 服务别名，实现多个接口时必须指定别名
* */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    String value() default "";
}
