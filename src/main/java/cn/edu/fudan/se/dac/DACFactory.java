package cn.edu.fudan.se.dac;

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

    public synchronized <T> DataAccessInterface<T> createDAC(Class<T> beanClass) {
        if(beanClass == null){
            throw new NullPointerException("beanClass is null");
        }
        DataAccessInterface<T> result = classDACMap.get(beanClass);
        if (result == null) {
            result = new FileBasedDataAccessComponent<T>(beanClass);
            classDACMap.put(beanClass, result);
        }
        return result;
    }
}
