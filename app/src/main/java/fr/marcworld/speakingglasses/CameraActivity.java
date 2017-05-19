package fr.marcworld.speakingglasses;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import fr.marcworld.speakingglasses.enums.SupportedLanguage;
import fr.marcworld.speakingglasses.utils.LocaleUtils;
import fr.marcworld.speakingglasses.views.CameraPreview;

/**
 * Allow the user to take a photo.
 *
 * @author Marc Plouhinec
 */
public class CameraActivity extends Activity {

    private Camera camera;
    private CameraPreview cameraPreview;
    private TextToSpeech textToSpeech = null;
    private volatile boolean textToSpeechReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the resources locale
        String languageExtra = getIntent().getStringExtra(SupportedLanguage.class.getSimpleName());
        final SupportedLanguage language = languageExtra == null ? SupportedLanguage.ENGLISH : SupportedLanguage.valueOf(languageExtra);
        LocaleUtils.setResourcesLocale(language.getLocale(), this);

        // Initialize the activity content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);

        // Load the TTS engine and explain to the user that he can take a picture
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.ERROR) {
                    Toast.makeText(CameraActivity.this, R.string.error_tts_engine, Toast.LENGTH_SHORT).show();
                } else {
                    textToSpeechReady = true;
                    textToSpeech.setLanguage(language.getLocale());
                    textToSpeech.speak(getResources().getString(R.string.take_photo_explanation), TextToSpeech.QUEUE_ADD, null);
                    openCamera();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textToSpeechReady) {
            openCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void openCamera() {
        try {
            camera = Camera.open();
        } catch (RuntimeException ex) {
            Toast.makeText(this, R.string.error_cannot_start_camera, Toast.LENGTH_SHORT).show();
            textToSpeech.speak(getResources().getString(R.string.error_cannot_start_camera), TextToSpeech.QUEUE_ADD, null);
        }

        if (camera != null) {
            cameraPreview.setCamera(camera);
        }
    }

    private void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
