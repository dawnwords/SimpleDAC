package cn.edu.fudan.se.dac.test;

import cn.edu.fudan.se.dac.BeanSetter;
import cn.edu.fudan.se.dac.Condition;
import cn.edu.fudan.se.dac.DACFactory;
import cn.edu.fudan.se.dac.DataAccessInterface;
import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        DataAccessInterface<Student> studentDAC = DACFactory.getInstance().createDAC(Student.class);
        studentDAC.beginTransaction();
        for (Student s : randomStudent(10)) {
            studentDAC.add(s);
        }
        studentDAC.commit();
        Condition<Student> condition = new Condition<Student>() {
            @Override
            public boolean assertBean(Student student) {
                return student.name.contains("王");
            }
        };
        for (Student s : studentDAC.selectByCondition(condition)) {
            System.out.println(s);
        }
        System.out.println("---------");
        studentDAC.updateByCondition(condition, new BeanSetter<Student>() {
            @Override
            public void set(Student student) {
                student.gender = true;
                student.id = "14212010005";
            }
        });
        for (Student s : studentDAC.selectByCondition(condition)) {
            System.out.println(s);
        }
        System.out.println("---------");
        studentDAC.deleteByCondition(condition);
        System.out.println(studentDAC.selectByCondition(new Condition.True<Student>()).size());
    }

    private static List<Student> randomStudent(int number) {
        List<Student> result = new LinkedList<Student>();
        Random r = new Random(924);
        for (int i = 0; i < number; i++) {
            String id = randomId(r, 10);
            String name = randomName(r);
            boolean gender = r.nextBoolean();
            List<Lecture> lectures = randomLecture(r);
            Student student = new Student(id, name, gender, lectures);
            result.add(student);
        }
        return result;
    }

    private static List<Lecture> randomLecture(Random r) {
        List<Lecture> result = new LinkedList<Lecture>();
        for (int i = 0; i < r.nextInt(3) + 5; i++) {
            result.add(new Lecture(randomId(r, 5), randomName(r), randomName(r)));
        }
        return result;
    }

    private static String randomId(Random r, int len) {
        String id = "";
        for (int i = 0; i < len; i++) {
            id += String.valueOf(randomInt(r, 0, 10));
        }
        return id;
    }

    private static String randomName(Random r) {
        final String[] names = {
                "孙祥彦", "白云汉", "曹凯", "陈方飞", "郭俊石", "雷建坤", "刘嵩", "陆健", "马洲骏", "彭声闻", "佘玉轩", "盛益彬", "汪洋", "王飞", "吴东", "闫可", "杨达一", "张一舟", "赵志洲", "郑健", "周孝佳", "周晔", "祝家烨", "陈济凡", "程文章", "冯兆华", "顾攀", "胡洋洋", "黄思渊", "黄文辉", "金蒿林", "李晨杰", "刘巍", "刘志鑫", "罗宏俊", "申金晟", "孙众毅", "王曦", "徐苒茨", "严鑫", "张天皓", "张志豪", "朱成纯", "陈坤", "陈松奎", "陈悠", "崔战伟", "范殊文", "何松阳", "姜经翔", "瞿鹏亮", "黎朋飛", "刘敦敏", "刘杰", "秦靖雅", "孙超", "吴军", "许家华", "杨骁", "杨奕臻", "易翔宇", "张高铭", "张军", "张周", "郑小青", "邹阳", "陈磊", "蒋恒", "王飞", "康积华", "林坚渤", "宋云涛", "王程玉", "徐卫东", "刘亮兴", "陈恒", "方喆然", "冯泽宇", "胡玥申", "李通", "李致公", "刘海涛", "申晨", "树岸", "谭力", "王海", "于翔", "张宓", "朱亚迪", "卞景浩", "贾嘉宁", "刘宇飞", "柴宁", "冯超逸", "顾敬潇", "李栋", "李丰宇", "刘凯", "马晓凯", "沈剑锋", "史橹", "王诗碕", "王欣", "武晓伟", "邢小璐", "徐日", "姚帆", "余时强", "张磊", "张时乐", "钟浙云", "胡笑颜", "冯国栋", "郑家欢", "陈新驰", "沈志强", "常玉虎", "井晓阳", "刘畅", "田凯", "朱海平", "刘鹏飞", "田璐超", "刘汶谏", "吴昊", "张晓寒", "邱堃", "张旭", "陈阳", "陈勇", "高启航", "葛文韬", "黄一夫", "姜里羊", "刘欢", "刘王胜", "覃华峥", "唐波", "唐福宇", "唐锦阳", "吴昊东", "杨恺希", "易宇豪", "郑诚", "郑肖雄", "周传杰", "杜正阳", "孙建江", "涂坚", "魏学才", "魏扬威", "吴世宇", "吴祖煊", "严立宇", "张骏", "朱梦哲", "朱文琰", "竺晨曦", "邹杨修", "黄蔚", "黄文轩", "李斌", "毛文辉", "牟正方", "倪海凌", "钱晟", "孙慧明", "孙旭扬", "汪圣涛", "王力冠", "王小刚", "袁建伟", "张昉华", "张希报", "周之敏", "朱勤恩"
        };
        return names[r.nextInt(names.length)];
    }

    private static int randomInt(Random r, int start, int end) {
        return r.nextInt(end - start) + start;
    }


    static class Student implements Serializable {
        String id;
        String name;
        boolean gender;
        List<Lecture> selectedLecture;

        public Student(String id, String name, boolean gender, List<Lecture> selectedLecture) {
            this.id = id;
            this.name = name;
            this.gender = gender;
            this.selectedLecture = selectedLecture;
        }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    static class Lecture implements Serializable {
        String id;
        String name;
        String teacher;

        public Lecture(String id, String name, String teacher) {
            this.id = id;
            this.name = name;
            this.teacher = teacher;
        }
    }
}
