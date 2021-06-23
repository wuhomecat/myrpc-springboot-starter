package wu.myrpc.server.register;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import wu.myrpc.annotation.InjectService;
import wu.myrpc.annotation.Service;
import wu.myrpc.client.ClientProxyFactory;
import wu.myrpc.server.RpcServer;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

/*
* RPC启动器，基于spring监听器，spring容器启动后，就会自动执行onApplicationEvent方法，让两个注解生效
* - 服务启动暴露：会将标注@Service的服务创建对应的服务类对象，注册到本地map和zookeeper上（服务提供方使用）
* - 自动注入Service：会对标注@InjectService的成员变量（接口实例化对象）做注入，注入的是它的代理对象（服务调用方使用）
* */
public class DefaultRpcProcessor implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegister.class);

    @Resource//按name注入，对比@Autowired按类型注入
    private ClientProxyFactory clientProxyFactory;

    @Resource
    private ServiceRegister serviceRegister;

    @Resource
    private RpcServer rpcServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (Objects.isNull(event.getApplicationContext().getParent())) {
            ApplicationContext context = event.getApplicationContext();
            // 开启服务
            startServer(context);

            // 注入Service
            injectService(context);
        }
    }

    //将标注@Service的服务类的bean对象进行解析，构造对应的ServiceObject对象，注册到本地map和zookeeper上
    private void startServer(ApplicationContext context) {
        //获取所有@Service标注的bean，也就是IOC容器中，所有已注册的服务接口实现类的bean对象
        Map<String, Object> beans = context.getBeansWithAnnotation(Service.class);
        if (beans.size() != 0) {
            boolean startServerFlag = true;
            //枚举所有服务实现类的bean对象
            for (Object obj : beans.values()) {
                try {
                    //获取bean的Class对象
                    Class<?> clazz = obj.getClass();
                    //获取bean实现的所有接口：因为一个类可以实现多个接口
                    Class<?>[] interfaces = clazz.getInterfaces();
                    ServiceObject so;//为每个bean对象构造一个ServiceObject对象
                    //如果接口数量>1：就要为服务取个别名
                    if (interfaces.length != 1) {
                        //获取@Service注解的属性value = 服务别名
                        Service service = clazz.getAnnotation(Service.class);
                        String value = service.value();
                        //如果没指定别名，则创建失败
                        if (value.equals("")) {
                            startServerFlag = false;
                            throw new UnsupportedOperationException("The exposed interface is not specific with '" + obj.getClass().getName() + "'");
                        }
                        //构造一个ServiceObject对象：参数对应{服务别名，服务类型，服务的实例对象}
                        so = new ServiceObject(value, Class.forName(value), obj);
                    }
                    //如果只有一个接口：就直接以父类类名作为服务名
                    else {
                        Class<?> superClass = interfaces[0];
                        so = new ServiceObject(superClass.getName(), superClass, obj);
                    }
                    serviceRegister.register(so);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (startServerFlag) {
                rpcServer.start();
            }
        }
    }

    //将标注@InjectService的成员变量注入对应的bean：
    //标注的是接口对象，注入的是它的动态代理对象。
    private void injectService(ApplicationContext context) {
        //枚举spring中所有的bean
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)) continue;
            //枚举该bean对象中的所有成员变量
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //获取标注@InjectService的成员变量
                InjectService injectLeisure = field.getAnnotation(InjectService.class);
                if (Objects.isNull(injectLeisure)) continue;
                Class<?> fieldClass = field.getType();
                logger.info("服务名：" + fieldClass.getName());
                Object object = context.getBean(name);
                field.setAccessible(true);
                try {
                    field.set(object, clientProxyFactory.getProxy(fieldClass));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}