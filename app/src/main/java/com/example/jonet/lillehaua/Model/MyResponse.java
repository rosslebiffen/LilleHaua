package com.example.jonet.lillehaua.Model;


import java.util.List;

import javax.xml.transform.Result;



public class MyResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;
}
