package fr.marcworld.speakingglasses;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import fr.marcworld.speakingglasses.enums.SupportedLanguage;
import fr.marcworld.speakingglasses.exceptions.ImageAnalysisFailedException;
import fr.marcworld.speakingglasses.exceptions.NoWebConnectionException;
import fr.marcworld.speakingglasses.services.CloudSightService;
import fr.marcworld.speakingglasses.services.impl.CloudSightServiceImpl;
import fr.marcworld.speakingglasses.utils.LocaleUtils;

/**
 * Analyze and describe the photo to the user.
 *
 * @author Marc Plouhinec
 */
public class PhotoAnalysisActivity extends Activity {

    public static final String INTENT_PHOTO_EXTRA = "PHOTO";
    private static final String TAG = "PhotoAnalysisActivity";

    private TextToSpeech textToSpeech = null;
    private final Properties applicationProperties = new Properties();
    private CloudSightService cloudSightService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handler = new Handler();

        // Set the resources locale
        String languageExtra = getIntent().getStringExtra(SupportedLanguage.class.getSimpleName());
        final SupportedLanguage language = languageExtra == null ? SupportedLanguage.ENGLISH : SupportedLanguage.valueOf(languageExtra);
        LocaleUtils.setResourcesLocale(language.getLocale(), this);

        // Load configuration
        InputStream inputStream = null;
        try {
            inputStream = getBaseContext().getAssets().open("application.properties");
            applicationProperties.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "Unable to load application.properties", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        // Get the cloudSightService
        String cloudSightApiKey = applicationProperties.getProperty("cloudSight.apiKey");
        cloudSightService = new CloudSightServiceImpl(cloudSightApiKey);

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

                    // Send the photo to Cloud Sight API to analyze the photo
                    Toast.makeText(PhotoAnalysisActivity.this, R.string.analyze_photo, Toast.LENGTH_SHORT).show();
                    textToSpeech.speak(getResources().getString(R.string.analyze_photo), TextToSpeech.QUEUE_ADD, null);
                    new AnalyzePhotoTask(photo, language).execute();
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

    private class AnalyzePhotoTask extends AsyncTask<String, Void, Void> {

        private final byte[] photo;
        private final SupportedLanguage language;

        private AnalyzePhotoTask(byte[] photo, SupportedLanguage language) {
            this.photo = photo;
            this.language = language;
        }

        @Override
        protected Void doInBackground(String... params) {
            // Analyse the image
            String message = "";
            try {
                message = cloudSightService.analyzeImage(photo, language);
            } catch (NoWebConnectionException e) {
                Log.w(TAG, e);
                message = getResources().getString(R.string.error_no_connectivity);
            } catch (ImageAnalysisFailedException e) {
                Log.w(TAG, e);
                message = getResources().getString(R.string.error_analysis_failed);
            }

            // Read the image description or the error
            final String finalMessage = message;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(PhotoAnalysisActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                    textToSpeech.speak(finalMessage, TextToSpeech.QUEUE_ADD, null);
                }
            });

            return null;
        }
    }
}
