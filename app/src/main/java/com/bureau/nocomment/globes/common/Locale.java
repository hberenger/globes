package com.bureau.nocomment.globes.common;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.bureau.nocomment.globes.R;

public enum Locale {
    ENGLISH ("en", R.id.menu_language_english),
    FRENCH  ("fr", R.id.menu_language_french),
    UNKNOWN ("??", 0);

    private final String code;
    private final int menuId;
    private static Locale current = UNKNOWN;

    Locale(String code, int menuId) {
        this.code = code;
        this.menuId = menuId;
    }

    public String getCode() {
        return code;
    }

    public int getMenuId() {
        return menuId;
    }

    public static Locale getCurrent(Context context) {
        if (current == UNKNOWN) {
            Resources res = context.getResources();
            Configuration conf = res.getConfiguration();
            String languageCode = conf.locale.getLanguage();
            if (languageCode.equals(ENGLISH.code)) {
                current = ENGLISH;
            } else if (languageCode.equals(FRENCH.code)) {
                current = FRENCH;
            }
        }
        return current;
    }

    // returns false when no error occured
    public Boolean setAsCurrent(Context context) {
        if (Locale.current == this) {
            return true;
        }
        java.util.Locale newLocale = new java.util.Locale(code);
        java.util.Locale.setDefault(newLocale);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(newLocale);
        res.updateConfiguration(conf, dm);
        Locale.current = this;
        return false;
    }
}
