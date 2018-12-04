package de.kah2.mondtag;

import android.app.Application;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.lang.reflect.Array;
import java.util.Arrays;

import de.kah2.mondtag.calendar.InterpreterMapper;
import de.kah2.mondtag.datamanagement.DataManager;

/**
 * This class exists to hold data that should always and anywhere be available.
 * Why I prefer this over using singletons:
 * http://stackoverflow.com/a/708317/6747171
 *
 * Created by kahles on 11.11.16.
 */
public class Mondtag extends Application {

    private final static String LOG_TAG = Mondtag.class.getSimpleName();

    private DataManager dataManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "######### onCreate called ############");

        AndroidThreeTen.init(this);

        InterpreterMapper.init(this);

        this.dataManager= new DataManager(this);
    }

    public DataManager getDataManager() {

        return dataManager;
    }
}
