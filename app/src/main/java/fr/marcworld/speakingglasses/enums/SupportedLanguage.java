package fr.marcworld.speakingglasses.enums;

import java.util.Locale;

/**
 * Supported language.
 *
 * @author Marc Plouhinec
 */
public enum SupportedLanguage {

    ENGLISH(Locale.ENGLISH, "English", "en-US", "en"),
    FRENCH(Locale.FRENCH, "Français", "fr-FR", "fr");

    private final Locale locale;
    private final String label;
    private final String tag;
    private final String isoCode;

    SupportedLanguage(Locale locale, String label, String tag, String isoCode) {
        this.locale = locale;
        this.label = label;
        this.tag = tag;
        this.isoCode = isoCode;
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

    public String getIsoCode() {
        return isoCode;
    }
}
