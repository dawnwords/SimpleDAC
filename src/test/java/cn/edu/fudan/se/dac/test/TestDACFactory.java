package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import cn.edu.fudan.se.dac.test.bean.Student;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertSame;

/**
 * Created by Dawnwords on 2015/6/24.
 */
public class TestDACFactory {
    @Test
    public void testInstance() {
        DataAccessInterface<Student> dac1 = DACFactory.getInstance().createDAC(Student.class);
        DataAccessInterface<Student> dac2 = DACFactory.getInstance().createDAC(Student.class);
        assertSame(dac1, dac2);
    }

    @After
    public void teardown() {
        new File("Student").deleteOnExit();
    }
}
