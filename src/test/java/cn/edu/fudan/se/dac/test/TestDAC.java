package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.BeanSetter;
import cn.edu.fudan.se.dac.Condition;
import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import cn.edu.fudan.se.dac.test.bean.Student;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.runners.TestClassRunner;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Dawnwords on 2015/5/29.
 */
@RunWith(TestClassRunner.class)
public class TestDAC {
    private static DataAccessInterface<Student> dac;
    private static Condition<Student> condition;
    private static BeanSetter<Student> setter;

    @BeforeClass
    public static void setup() {
        dac = DACFactory.getInstance().createDAC(Student.class);
        condition = new Condition<Student>() {
            @Override
            public boolean assertBean(Student student) {
                return student.getName().contains("a");
            }
        };
        setter = new BeanSetter<Student>() {
            @Override
            public void set(Student student) {
                student.setGender(true);
                student.setId("14212010005");
            }
        };
    }

    @Test
    public void testAddStudent() {
        final int studentNum = 50;

        dac.beginTransaction();
        for (Student student : StudentGenerator.getInstance().randomStudent(studentNum)) {
            dac.add(student);
        }
        dac.commit();

        assertEquals(studentNum, dac.selectByCondition(Condition.True).size());
    }

    @Test
    public void testUpdateStudent() {
        dac.updateByCondition(condition, setter);
        for (Student student : dac.selectByCondition(condition)) {
            assertTrue(student.isGender());
            assertEquals("14212010005", student.getId());
        }
    }

    @Test
    public void testDeleteStudent() {
        dac.deleteByCondition(condition);
        for (Student student : dac.selectByCondition(Condition.True)) {
            assertTrue(!condition.assertBean(student));
        }
    }

    @AfterClass
    public static void teardown() {
        new File(Student.class.getSimpleName()).deleteOnExit();
    }
}
