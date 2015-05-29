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
}