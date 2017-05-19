package fr.marcworld.speakingglasses;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.widget.Toast;

import com.reconinstruments.os.HUDOS;
import com.reconinstruments.os.connectivity.HUDConnectivityManager;

import fr.marcworld.speakingglasses.enums.SupportedLanguage;
import fr.marcworld.speakingglasses.utils.LocaleUtils;

/**
 * Analyze and describe the photo to the user.
 *
 * @author Marc Plouhinec
 */
public class PhotoAnalysisActivity extends Activity {

    public static final String INTENT_PHOTO_EXTRA = "PHOTO";

    private TextToSpeech textToSpeech = null;
    private HUDConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the resources locale
        String languageExtra = getIntent().getStringExtra(SupportedLanguage.class.getSimpleName());
        final SupportedLanguage language = languageExtra == null ? SupportedLanguage.ENGLISH : SupportedLanguage.valueOf(languageExtra);
        LocaleUtils.setResourcesLocale(language.getLocale(), this);

        // Load the connectivity manager
        connectivityManager = (HUDConnectivityManager) HUDOS.getHUDService(HUDOS.HUD_CONNECTIVITY_SERVICE);

        // Initialize the activity content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_analysis);

        // Display the photo
        final byte[] photo = getIntent().getByteArrayExtra(INTENT_PHOTO_EXTRA);
        ImageView photoImageView = (ImageView) findViewById(R.id.photoImageView);
        photoImageView.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length));

        // Load the TTS engine
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.ERROR) {
                    Toast.makeText(PhotoAnalysisActivity.this, R.string.error_tts_engine, Toast.LENGTH_SHORT).show();
                } else {
                    textToSpeech.setLanguage(language.getLocale());
                    startPhotoAnalysis(photo);
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

    private void startPhotoAnalysis(byte[] photo) {

        // Check we have internet connectivity
        if (!connectivityManager.hasWebConnection()) {
            Toast.makeText(PhotoAnalysisActivity.this, R.string.error_no_connectivity, Toast.LENGTH_SHORT).show();
            textToSpeech.speak(getResources().getString(R.string.error_no_connectivity), TextToSpeech.QUEUE_ADD, null);
            return;
        }

        // Send the photo to Cloud Sight API to analyze the photo
        Toast.makeText(PhotoAnalysisActivity.this, R.string.analyze_photo, Toast.LENGTH_SHORT).show();
        textToSpeech.speak(getResources().getString(R.string.analyze_photo), TextToSpeech.QUEUE_ADD, null);
        // TODO
    }
}
