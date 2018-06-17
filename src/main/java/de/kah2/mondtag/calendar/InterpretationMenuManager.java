package de.kah2.mondtag.calendar;

import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.SortedMap;
import java.util.TreeMap;

import de.kah2.libZodiac.interpretation.Gardening;
import de.kah2.libZodiac.interpretation.Interpreter;
import de.kah2.libZodiac.interpretation.Translatable;

/**
 * TODO docs
 */

public class InterpretationMenuManager implements PopupMenu.OnMenuItemClickListener {

    private final static String TAG = InterpretationMenuManager.class.getSimpleName();

    private final static SortedMap<String, Class<? extends Interpreter>> gardeningInterpreterClasses
            = new TreeMap<>();

    static {
        for (Class<?> clazz : Gardening.class.getDeclaredClasses()) {

            final Class<? extends Interpreter> interpreterClass = clazz.asSubclass(Interpreter.class);

            gardeningInterpreterClasses.put(
                    Translatable.getKey( interpreterClass.getName() ),
                    interpreterClass );
        }
    }

    public void addInterpreters(Menu menu) {

        menu.clear();

        for (String key : gardeningInterpreterClasses.keySet()) {

            final Integer[] resourceIds = ResourceMapper.getResourceIds(key);

            if (resourceIds == null) {
                Log.e(TAG, "No Resource mapped to interpreter with key \"" + key
                        + "\" - using key as text to display.");

                menu.add(key);
            } else {
                // FIXME wie sollen die menuitems hinzugefügt und anschl. gemapped werden?
                menu.add( resourceIds[ResourceMapper.INDEX_STRING] );
                // menu.add( GROUP_ID, ID, ORDER, resourceIds[ResourceMapper.INDEX_STRING] )
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        item.setChecked(true);
        Log.d(TAG, "onMenuItemClick: id:" + item.getItemId() + " --> " + item.toString());

        // TODO handle interpreters

        return false;
    }
}
