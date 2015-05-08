package parking.group6.csc413.projectmap;

import android.app.Application;

/**
 * Created by Swati on 5/8/2015.
 */
public class Global_var extends Application {

    private static int myParkedState = 0;

    public static int getState(){
        return myParkedState;
    }
    public static void setState(int s){
        myParkedState = s;
    }
}
