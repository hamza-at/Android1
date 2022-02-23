package com.example.geosafe;

import android.app.Application;

import com.example.geosafe.model.User;

public class ClientAndroid extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
