package fr.marcworld.speakingglasses;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import com.reconinstruments.ui.list.SimpleListActivity;
import com.reconinstruments.ui.list.StandardListItem;

import java.util.Locale;

import fr.marcworld.speakingglasses.enums.SupportedLanguage;

/**
 * Application entry-point. Allow a user to select a language.
 *
 * @author Marc Plouhinec
 */
public class MainActivity extends SimpleListActivity {

    private TextToSpeech textToSpeech = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_standard_layout);

        // Load the TTS engine and ask the user to choose a language
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.ERROR) {
                    // Display an error message
                    setContents(new StandardListItem(getResources().getString(R.string.error_tts_engine)));
                } else {
                    // Ask the user to choose a language
                    setResourcesLocale(Locale.ENGLISH);
                    textToSpeech.setLanguage(Locale.ENGLISH);
                    textToSpeech.speak(getResources().getString(R.string.please_choose_language), TextToSpeech.QUEUE_ADD, null);

                    setResourcesLocale(Locale.FRENCH);
                    textToSpeech.setLanguage(Locale.FRENCH);
                    textToSpeech.speak(getResources().getString(R.string.please_choose_language), TextToSpeech.QUEUE_ADD, null);

                    // Show a list of languages the user must select
                    setContents(new LanguageListItem(SupportedLanguage.ENGLISH), new LanguageListItem(SupportedLanguage.FRENCH));
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void setResourcesLocale(Locale locale) {
        Resources resources = this.getResources();
        resources.getConfiguration().locale = locale;
        resources.updateConfiguration(resources.getConfiguration(), null);
    }

    /**
     * Handle language events.
     */
    private class LanguageListItem extends StandardListItem {

        private final SupportedLanguage language;

        private LanguageListItem(SupportedLanguage language) {
            super(language.getLabel());
            this.language = language;
        }

        @Override
        public void onSelected(Context context) {
            // Read the language when selected
            textToSpeech.setLanguage(language.getLocale());
            textToSpeech.speak(language.getLabel(), TextToSpeech.QUEUE_ADD, null);
        }

        @Override
        public void onClick(Context context) {
            setResourcesLocale(language.getLocale());
            // TODO
        }
    }

}
