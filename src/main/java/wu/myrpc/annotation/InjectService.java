package wu.myrpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/*
* 该注解用于服务接口实现类对象作为成员变量时做注入，在客户端使用
* */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface InjectService {
}
