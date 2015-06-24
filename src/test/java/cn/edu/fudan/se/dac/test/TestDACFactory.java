package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import cn.edu.fudan.se.dac.test.bean.Lecture;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * Created by Dawnwords on 2015/6/24.
 */
public class TestDACFactory {
    @Test
    public void testInstance() {
        DataAccessInterface<Lecture> dac1 = DACFactory.getInstance().createDAC(Lecture.class);
        DataAccessInterface<Lecture> dac2 = DACFactory.getInstance().createDAC(Lecture.class);
        assertSame(dac1, dac2);
    }
}
