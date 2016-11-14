package net.karthikraj.todobestpractices.utils;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

/**
 * Created by karthik on 10/11/16.
 */

public class TodoApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }
}
