package de.kah2.mondtag.calendar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.PopupMenu;

import de.kah2.mondtag.R;
import de.kah2.zodiac.libZodiac4A.interpretation.Interpreter;

/**
 * Handles the menu that is used to switch {@link Interpreter}s.
 */
public class InterpretationMenuManager implements PopupMenu.OnMenuItemClickListener {

    private final static String TAG = InterpretationMenuManager.class.getSimpleName();

    private InterpretationChangeListener interpretationChangeListener;

    void addInterpreters(Menu menu, int selectedInterpreterId) {

        int order = 0;

        MenuItem actualItem = menu.add( R.id.menu_interpretations_check_group,
                R.string.interpret_none, order, R.string.interpret_none );
        actualItem.setChecked( R.string.interpret_none == selectedInterpreterId );

        for (int id : InterpreterManager.getIds()) {

            order++;

            actualItem = menu.add( R.id.menu_interpretations_check_group, id, order, id );
            actualItem.setChecked( id == selectedInterpreterId );
        }

        menu.setGroupCheckable( R.id.menu_interpretations_check_group, true, true );
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
