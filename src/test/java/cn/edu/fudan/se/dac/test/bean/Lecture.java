package cn.edu.fudan.se.dac.test.bean;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

/**
 * Created by Dawnwords on 2015/5/29.
 */
public class Lecture implements Serializable {
    private String id;
    private String name;
    private String teacher;

    public Lecture() {
    }

    public Lecture(String id, String name, String teacher) {
        this.id = id;
        this.name = name;
        this.teacher = teacher;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lecture lecture = (Lecture) o;

        return id.equals(lecture.id) && name.equals(lecture.name) && teacher.equals(lecture.teacher);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + teacher.hashCode();
        return result;
    }
}