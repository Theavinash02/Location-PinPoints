package com.example.demo1;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class myApplication extends Application {
    private static myApplication singleton;

    private List<Location> myLocations;

    public static myApplication getSingleton() {
        return singleton;
    }

    public static void setSingleton(myApplication singleton) {
        myApplication.singleton = singleton;
    }

    public List<Location> getMyLocations(){
        return myLocations;
    }
    public void setMyLocations(List<Location> myLocations){
        this.myLocations = myLocations;
    }
    public myApplication getInstance(){
        return singleton;
    }
    public void onCreate(){
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
    }
}
