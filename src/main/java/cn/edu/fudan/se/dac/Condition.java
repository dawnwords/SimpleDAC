package cn.edu.fudan.se.dac;

/**
 * Created by Dawnwords on 2015/5/25.
 */
public interface Condition<Bean> {
    boolean assertBean(Bean bean);

    Condition True = new Condition() {
        @Override
        public boolean assertBean(Object bean) {
            return true;
        }
    };

}