package fr.marcworld.speakingglasses.enums;

import java.util.Locale;

/**
 * Supported language.
 *
 * @author Marc Plouhinec
 */
public enum SupportedLanguage {

    ENGLISH(Locale.ENGLISH, "English", "en"),
    FRENCH(Locale.FRENCH, "Français", "fr");

    private final Locale locale;
    private final String label;
    private final String tag;

    SupportedLanguage(Locale locale, String label, String tag) {
        this.locale = locale;
        this.label = label;
        this.tag = tag;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLabel() {
        return label;
    }

    public String getTag() {
        return tag;
    }
}
