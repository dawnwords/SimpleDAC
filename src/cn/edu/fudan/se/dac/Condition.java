package cn.edu.fudan.se.dac;

/**
 * Created by Dawnwords on 2015/5/25.
 */
public interface Condition<Bean> {
    boolean assertBean(Bean bean);

    class True<Bean> implements Condition<Bean>{
        @Override
        public boolean assertBean(Bean bean) {
            return true;
        }
    }

}