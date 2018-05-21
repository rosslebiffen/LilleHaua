package com.example.jonet.lillehaua.Model;

import java.util.Map;

/**
 * Created by Jone on 15.05.2018.
 */

public class DataMessage {
    public String to;
    public Map<String,String> data;

    public DataMessage(String to) {
        this.to = to;
    }

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
