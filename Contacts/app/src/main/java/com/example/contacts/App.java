package com.example.contacts;

import android.app.Application;

import androidx.room.Room;

public class App extends Application {

    public static App app;
    public DB db;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        db = Room.databaseBuilder(this, DB.class, "db").fallbackToDestructiveMigration().build();
    }
}
