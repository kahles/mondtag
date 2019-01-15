package de.kah2.mondtag;

import android.app.Application;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

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

    /**
     * Helper-method to examine e.g. where a listener gets called.
     * @return the stack trace where this method was called
     * @param depth number of stack trace elements to append
     */
    public static String getStackTrace(int depth) {

        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        final int offset = 3;
        int max = offset + depth;

        if (max > trace.length) {
            max = trace.length;
        }

        final StringBuilder builder = new StringBuilder();

        for (int line = offset; line < max; line ++) {

            final StackTraceElement el = trace[line];

            if (line > offset) {
                builder.append("\t@");
            }

            builder.append(el.getClassName());
            builder.append("#");
            builder.append(el.getMethodName());
            builder.append(":");
            builder.append(el.getLineNumber());
            builder.append("\n");
        }

        return builder.toString();
    }
}
