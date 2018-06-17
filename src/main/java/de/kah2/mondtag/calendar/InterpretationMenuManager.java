package de.kah2.mondtag.calendar;

import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Hashtable;
import java.util.SortedMap;
import java.util.TreeMap;

import de.kah2.libZodiac.Calendar;
import de.kah2.libZodiac.interpretation.Gardening;
import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.libZodiac.interpretation.Translatable;
import de.kah2.mondtag.R;
import de.kah2.mondtag.datamanagement.DataManager;

/**
 * TODO docs
 */

public class InterpretationMenuManager implements PopupMenu.OnMenuItemClickListener {

    private final static String TAG = InterpretationMenuManager.class.getSimpleName();

    private static Hashtable<Integer, Class<? extends Interpreter>> idMap = new Hashtable<>();

    // TODO add option for "none"

    static {
        idMap.put(R.string.interpret_gardening_CombatSlugs, Gardening.CombatSlugsInterpreter.class);
        idMap.put(R.string.interpret_gardening_CutFruitTree, Gardening.CutFruitTreeInterpreter.class);
        idMap.put(R.string.interpret_gardening_Cutting, Gardening.CuttingInterpreter.class);
        idMap.put(R.string.interpret_gardening_Graft, Gardening.GraftInterpreter.class);
        idMap.put(R.string.interpret_gardening_MowLawn, Gardening.MowLawnInterpreter.class);
        idMap.put(R.string.interpret_gardening_OverterrestrialPests, Gardening.OverterrestrialPestsInterpreter.class);
        idMap.put(R.string.interpret_gardening_SubterrestrialPests, Gardening.SubterrestrialPestsInterpreter.class);
        idMap.put(R.string.interpret_gardening_TrimSick, Gardening.TrimSickInterpreter.class);
        idMap.put(R.string.interpret_gardening_Water, Gardening.WaterInterpreter.class);
        idMap.put(R.string.interpret_gardening_WeedDig, Gardening.WeedDigInterpreter.class);
    }

    private InterpretationChangeListener interpretationChangeListener;

    public void addInterpreters(Menu menu) {

        menu.clear();

        for (int id : idMap.keySet()) {

            // TODO_LATER order by display name
            final int ORDER = 0;

            menu.add( Menu.NONE, id, ORDER, id );
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        item.setChecked(true);

        Log.d(TAG, "onMenuItemClick: id:" + item.getItemId() + " --> " + item.toString());

        final Class <? extends Interpreter> selectedInterpreterClass = idMap.get( item.getItemId() );

        if ( selectedInterpreterClass == null ) {
            Log.e( TAG, "onMenuItemClick: unknown interpreter" );
            return false;
        }

        if (interpretationChangeListener != null) {
            this.interpretationChangeListener.onInterpreterChanged(
                    item.getItemId(),
                    selectedInterpreterClass );
        }

        return true;
    }

    public void setInterpretationChangeListener(InterpretationChangeListener interpretationChangeListener) {
        Log.d(TAG, "setInterpretationChangeListener called");
        this.interpretationChangeListener = interpretationChangeListener;
    }

    public void resetInterpretationChangeListener() {
        Log.d(TAG, "resetInterpretationChangeListener called");
        this.interpretationChangeListener = null;
    }

    public interface InterpretationChangeListener {

        void onInterpreterChanged(int nameResId, Class <? extends Interpreter> clazz);
    }
}
