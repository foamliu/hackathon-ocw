package org.hackathon_ocw.androidclient.domain;

import java.util.Date;

/**
 * Created by Foam on 2016/6/1.
 */
public class HistoryEntry {
    public long userId;         //
    public Course course;       // 课程
    public Date watchedTime;    // 什么时间看的
    public int position;        // 看到哪里，以秒计
}
