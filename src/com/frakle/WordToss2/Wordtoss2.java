package com.frakle.WordToss2;

import android.app.Application;
import android.content.Context;

public class Wordtoss2 extends Application{

    private static Context context;

    public void onCreate(){
    	Wordtoss2.context=getApplicationContext();
    }

    public static Context getAppContext(){
    	return context;
    }
}