package cn.edu.fudan.se.dac;

import java.util.List;

/**
 * Created by Dawnwords on 2015/5/21.
 */
public interface DataAccessInterface<Bean> {
    boolean beginTransaction();

    boolean commit();

    boolean add(Bean bean);

    boolean deleteByCondition(Condition<Bean> condition, Class<Bean> beanClass);

    boolean updateByCondition(Condition<Bean> condition, Class<Bean> beanClass, BeanSetter<Bean> setter);

    List<Bean> selectByCondition(Condition<Bean> condition, Class<Bean> beanClass);

    interface Condition<Bean> {
        boolean assertBean(Bean bean);
    }

    interface BeanSetter<Bean> {
        void set(Bean bean);
    }
}
