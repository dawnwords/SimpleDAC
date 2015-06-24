package cn.edu.fudan.se.dac;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dawnwords on 2015/5/22.
 */
public class DACFactory {
    private static DACFactory instance;

    private ConcurrentHashMap<Class, DataAccessInterface> classDACMap;

    private DACFactory() {
        classDACMap = new ConcurrentHashMap<Class, DataAccessInterface>();
    }

    public static DACFactory getInstance() {
        return instance == null ? (instance = new DACFactory()) : instance;
    }

    public <T> DataAccessInterface<T> createDAC(Class<T> beanClass) {
        DataAccessInterface<T> result = classDACMap.get(beanClass);
        if (result == null) {
            result = (DataAccessInterface<T>) new DACHandler().newProxyInstance(new FileBasedDataAccessComponent<T>(beanClass));
            classDACMap.put(beanClass, result);
        }
        return result;
    }

    private class DACHandler implements InvocationHandler {
        private Object target;

        public Object newProxyInstance(Object targetObject) {
            this.target = targetObject;
            Class targetClass = targetObject.getClass();
            return Proxy.newProxyInstance(targetClass.getClassLoader(), targetClass.getInterfaces(), this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().contains("ByCondition")) {
                Object condition = args[0];
                if (condition == null) {
                    throw new RuntimeException("condition can not be null");
                }
            }
            return method.invoke(target, args);
        }
    }
}
