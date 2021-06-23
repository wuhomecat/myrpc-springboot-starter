package wu.myrpc.common.serializer;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.charset.StandardCharsets;

/*
* 枚举序列化算法，现支持：jdk, fastjson
*
* */
public enum  Serializer implements ISerializer {
    JAVA
    {
        //使用JDK原生的序列化算法
        @Override
        public <T > byte[] serialize (T object){
        //序列化后的字节数组
        byte[] bytes = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

        @Override
        public <T > T deserialize(Class < T > clazz, byte[] bytes){
        //反序列化后的对象
        T target = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            target = (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return target;
    }
    },

    // fastjson的序列化和反序列化
    FASTJSON
    {
        @Override
        public <T > byte[] serialize (T object){
            String s = JSON.toJSONString(object);
            // 指定字符集，获得字节数组
            return s.getBytes(StandardCharsets.UTF_8);
    }

        @Override
        public <T > T deserialize(Class < T > clazz, byte[] bytes){
            String s = new String(bytes, StandardCharsets.UTF_8);
            // 此处的clazz为具体类型的Class对象，而不是父类Message的
            return JSON.parseObject(s, clazz);
        }
    };
}
