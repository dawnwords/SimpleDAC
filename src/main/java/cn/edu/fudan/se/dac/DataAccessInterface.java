package cn.edu.fudan.se.dac;

import java.util.List;

/**
 * Created by Dawnwords on 2015/5/21.
 */
public interface DataAccessInterface<Bean> {
    boolean beginTransaction();

    boolean commit();

    boolean rollback();

    boolean add(Bean bean);

    boolean deleteByCondition(Condition<Bean> condition);

    boolean updateByCondition(Condition<Bean> condition, BeanSetter<Bean> setter);

    List<Bean> selectByCondition(Condition<Bean> condition);

}
