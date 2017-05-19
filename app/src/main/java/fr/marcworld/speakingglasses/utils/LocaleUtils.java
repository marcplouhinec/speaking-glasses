package fr.marcworld.speakingglasses.utils;

import android.content.Context;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Utility functions around {@link Locale}.
 *
 * @author Marc Plouhinec
 */
public class LocaleUtils {

    /**
     * Set the current locale for the resources (strings, ...).
     */
    public static void setResourcesLocale(Locale locale, Context context) {
        Resources resources = context.getResources();
        resources.getConfiguration().locale = locale;
        resources.updateConfiguration(resources.getConfiguration(), null);
    }
}
