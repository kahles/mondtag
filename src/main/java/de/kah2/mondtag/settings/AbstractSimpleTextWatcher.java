package de.kah2.mondtag.settings;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * This is a simplification of {@link TextWatcher} to avoid boiler plate code 😞
 */
public abstract class AbstractSimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public abstract void afterTextChanged(Editable s);
}
