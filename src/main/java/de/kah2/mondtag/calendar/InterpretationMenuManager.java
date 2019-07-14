package de.kah2.mondtag.calendar;

import androidx.appcompat.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.kah2.mondtag.R;

/**
 * Handles the menu that is used to switch {@link de.kah2.libZodiac.interpretation.Interpreter}s.
 */
public class InterpretationMenuManager implements PopupMenu.OnMenuItemClickListener {

    private final static String TAG = InterpretationMenuManager.class.getSimpleName();

    private InterpretationChangeListener interpretationChangeListener;

    void addInterpreters(Menu menu) {

        menu.clear();

        int order = 0;

        menu.add(Menu.NONE, R.string.interpret_none, order, R.string.interpret_none);

        for (int id : InterpreterManager.getIds()) {

            order++;

            menu.add( Menu.NONE, id, order, id );
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        item.setChecked(true);

        Log.d(TAG, "onMenuItemClick: id:" + item.getItemId() + " --> " + item.toString());

        if (item.getItemId() == R.string.interpret_none) {
            this.interpretationChangeListener.onInterpreterChanged(null);
            return true;
        }

        MappedInterpreter interpreter = InterpreterManager.getInterpreter( item.getItemId() );

        if ( interpreter == null ) {
            Log.e( TAG, "onMenuItemClick: unknown interpreter" );
            return false;
        }

        if (interpretationChangeListener != null) {
            this.interpretationChangeListener.onInterpreterChanged(interpreter);
        } else {
            Log.e(TAG, "onMenuItemClick: no listener available" );
        }

        return true;
    }

    void setInterpretationChangeListener(InterpretationChangeListener interpretationChangeListener) {
        Log.d(TAG, "setInterpretationChangeListener called");
        this.interpretationChangeListener = interpretationChangeListener;
    }

    void resetInterpretationChangeListener() {
        Log.d(TAG, "resetInterpretationChangeListener called");
        this.interpretationChangeListener = null;
    }

    public interface InterpretationChangeListener {

        void onInterpreterChanged(MappedInterpreter interpreter);
    }
}
