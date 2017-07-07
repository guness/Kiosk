package com.guness.kiosk.service.model;

import com.guness.kiosk.service.utils.firebase.FirebaseKeys;

import java.util.List;

/**
 * Created by guness on 02/07/2017.
 */
public class Job {

    public int id;
    @FirebaseKeys.JobTypes
    public String type;
    public List<String> result;
    public String status;
    public String key;
}
