package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.BeanSetter;
import cn.edu.fudan.se.dac.Condition;
import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import cn.edu.fudan.se.dac.test.bean.Student;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Dawnwords on 2015/5/29.
 */
public class TestDAC {
    private DataAccessInterface<Student> dac;
    private Condition<Student> condition;
    private BeanSetter<Student> setter;

    @Before
    public void setup() {
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

    @Test
    public void testMultiThreading() throws InterruptedException {
        final int studentNum = 10;
        final int threadNum = 100;
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (Student student : StudentGenerator.getInstance().randomStudent(studentNum)) {
                        dac.add(student);
                    }
                }
            };
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(threadNum * studentNum, dac.selectByCondition(Condition.True).size());
    }

    @After
    public void teardown() {
        boolean deleted = new File("Student").delete();
        System.out.println(deleted);
    }
}
