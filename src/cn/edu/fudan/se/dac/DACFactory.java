package cn.edu.fudan.se.dac;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Dawnwords on 2015/5/22.
 */
public class DACFactory {
    private static DACFactory instance;

    private DACFactory() {
    }

    public static DACFactory getInstance() {
        return instance == null ? (instance = new DACFactory()) : instance;
    }

    public <T> DataAccessInterface<T> newInstance(Class<T> beanClass) {
        FileBasedDataAccessComponent<T> result = new FileBasedDataAccessComponent<T>(beanClass.getSimpleName());
        return (DataAccessInterface<T>) new DACHandler().newProxyInstance(result);
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
            if (method.getName().contains("ByField")) {
                Object fieldName = args[0];
                Object beanClass = args[2];
                if (fieldName == null) {
                    throw new RuntimeException("fieldName can not be null");
                }
                if (beanClass == null) {
                    throw new RuntimeException("beanClass can not be null");
                }
            }
            return method.invoke(target, args);
        }
    }
}
