package org.hackathon_ocw.androidclient.domain;

/**
 * Created by dianyang on 2016/2/28.
 */
public class Course {
    long itemid;
    String title;
    String description;
    String piclink;
    String courselink;
    String webUrl;

    public Course() {

    }

    public Course(long id, String title, String desc, String piclink, String courselink, String webUrl) {
        this.itemid = id;
        this.title = title;
        this.description = desc;
        this.piclink = piclink;
        this.courselink = courselink;
        this.webUrl = webUrl;
    }

    public long getItemid() {
        return itemid;
    }

    public void setItemid(long itemid) {
        this.itemid = itemid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPiclink() {
        return piclink;
    }

    public void setPiclink(String piclink) {
        this.piclink = piclink;
    }

    public String getCourselink() {
        return courselink;
    }

    public void setCourselink(String courselink) {
        this.courselink = courselink;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}
