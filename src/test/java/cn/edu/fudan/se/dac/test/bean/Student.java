package cn.edu.fudan.se.dac.test.bean;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Dawnwords on 2015/5/29.
 */
public class Student implements Serializable {
    private String id;
    private String name;
    private boolean gender;
    private List<Lecture> selectedLecture;

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

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public List<Lecture> getSelectedLecture() {
        return selectedLecture;
    }

    public void setSelectedLecture(List<Lecture> selectedLecture) {
        this.selectedLecture = selectedLecture;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}