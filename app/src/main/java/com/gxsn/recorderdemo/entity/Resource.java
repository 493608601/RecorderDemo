package com.gxsn.recorderdemo.entity;

/**
 * Created by Administrator on 2017/4/6.
 */

public class Resource {
    public final static int TYPE_VIDEO = 0;
    public final static int TYPE_AUDIO = 1;
    private String name;
    private String time;
    private int duration;
    private int type;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
