package cn.edu.fudan.se.dac;

import java.util.List;

/**
 * Created by Dawnwords on 2015/5/21.
 */
public interface DataAccessInterface<Bean> {
    boolean beginTransaction();

    boolean commit();

    boolean add(Bean bean);

    boolean deleteByField(String fieldName, Object value, Class<Bean> beanClass);

    boolean updateByField(String selectFieldName, Object selectValue, Class<Bean> beanClass,
                          String updateFieldName, Object updateValue);

    List<Bean> selectByField(String fieldName, Object value, Class<Bean> beanClass);
}
