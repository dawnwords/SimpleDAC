package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.Condition;
import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import cn.edu.fudan.se.dac.test.bean.Student;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Dawnwords on 2015/6/25.
 */
public class TestDACConcurrency {

    @Test
    public void testTransactionAdd() throws InterruptedException {
        List<Thread> threads = new ArrayList<Thread>();

        int threadNum = 10;
        final int count = 100;
        final DataAccessInterface<Student> dac = DACFactory.getInstance().createDAC(Student.class);

        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    List<Student> initStudents = StudentGenerator.getInstance().randomStudent(count);
                    dac.beginTransaction();
                    for (Student student : initStudents) {
                        dac.add(student);
                    }
                    dac.commit();
                }
            };
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        assertEquals(threadNum * count, dac.selectByCondition(Condition.True).size());
    }

    @Test
    public void transactionWithNormal() {
        final int count = 100;
        final DataAccessInterface<Student> dac = DACFactory.getInstance().createDAC(Student.class);
        List<Student> initStudents = StudentGenerator.getInstance().randomStudent(count);
        for (Student student : initStudents) {
            dac.add(student);
        }
        assertEquals(count, dac.selectByCondition(Condition.True).size());

        new Thread() {
            @Override
            public void run() {
                dac.beginTransaction();
                List<Student> initStudents = StudentGenerator.getInstance().randomStudent(count);
                for (Student student : initStudents) {
                    dac.add(student);
                }
                try {
                    sleep(3100);
                } catch (InterruptedException ignored) {
                }
                dac.commit();
            }
        }.start();

        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            assertEquals(count, dac.selectByCondition(Condition.True).size());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        assertEquals(count + count, dac.selectByCondition(Condition.True).size());
    }

    @After
    public void teardown() {
        System.out.println(new File(Student.class.getSimpleName()).delete());
    }
}
