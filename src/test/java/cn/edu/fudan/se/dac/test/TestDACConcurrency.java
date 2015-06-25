package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.BeanSetter;
import cn.edu.fudan.se.dac.Condition;
import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import cn.edu.fudan.se.dac.test.bean.Lecture;
import cn.edu.fudan.se.dac.test.bean.Student;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void transactionWithDifferentTypeActions() throws InterruptedException {
        final DataAccessInterface<Lecture> dac = DACFactory.getInstance().createDAC(Lecture.class);
        dac.add(new Lecture("1", "1", "1"));
        dac.beginTransaction();
        dac.add(new Lecture("1", "1", "1"));
        dac.add(new Lecture("2", "2", "2"));
        dac.add(new Lecture("3", "3", "3"));
        dac.deleteByCondition(new Condition<Lecture>() {
            @Override
            public boolean assertBean(Lecture lecture) {
                return lecture.getId().equals("1");
            }
        });
        dac.updateByCondition(new Condition<Lecture>() {
            @Override
            public boolean assertBean(Lecture lecture) {
                return lecture.getId().equals("2");
            }
        }, new BeanSetter<Lecture>() {
            @Override
            public void set(Lecture lecture) {
                lecture.setTeacher("haha");
            }
        });
        new Thread() {
            @Override
            public void run() {
                List<Lecture> lectures = dac.selectByCondition(Condition.True);
                assertEquals(1, lectures.size());
                assertTrue(lectures.contains(new Lecture("1", "1", "1")));
            }
        }.start();
        Thread.sleep(1000);
        dac.add(new Lecture("2", "4", "4"));

        List<Lecture> lectures = dac.selectByCondition(Condition.True);
        assertEquals(3, lectures.size());
        assertTrue(lectures.contains(new Lecture("2", "2", "haha")));
        assertTrue(lectures.contains(new Lecture("3", "3", "3")));
        assertTrue(lectures.contains(new Lecture("2", "4", "4")));

        dac.add(new Lecture("4", "5", "5"));
        dac.deleteByCondition(new Condition<Lecture>() {
            @Override
            public boolean assertBean(Lecture lecture) {
                return lecture.getId().equals("2");
            }
        });

        lectures = dac.selectByCondition(Condition.True);
        assertEquals(2, lectures.size());
        assertTrue(lectures.contains(new Lecture("3", "3", "3")));
        assertTrue(lectures.contains(new Lecture("4", "5", "5")));
        new Thread() {
            @Override
            public void run() {
                List<Lecture> lectures = dac.selectByCondition(Condition.True);
                assertEquals(1, lectures.size());
                assertTrue(lectures.contains(new Lecture("1", "1", "1")));
            }
        }.start();
        Thread.sleep(1000);
        dac.commit();
        new Thread() {
            @Override
            public void run() {
                List<Lecture> lectures = dac.selectByCondition(Condition.True);
                assertEquals(2, lectures.size());
                assertTrue(lectures.contains(new Lecture("3", "3", "3")));
                assertTrue(lectures.contains(new Lecture("4", "5", "5")));
            }
        }.start();
    }

    @After
    public void teardown() {
        System.out.println(new File(Student.class.getSimpleName()).delete());
        System.out.println(new File(Lecture.class.getSimpleName()).delete());
    }
}
