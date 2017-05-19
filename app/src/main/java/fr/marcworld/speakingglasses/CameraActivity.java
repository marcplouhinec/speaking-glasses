package fr.marcworld.speakingglasses;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
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

    private SupportedLanguage language = SupportedLanguage.ENGLISH;
    private Camera camera;
    private CameraPreview cameraPreview;
    private TextToSpeech textToSpeech = null;
    private boolean textToSpeechReady = false;
    private boolean currentlyTakingPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the resources locale
        String languageExtra = getIntent().getStringExtra(SupportedLanguage.class.getSimpleName());
        language = languageExtra == null ? SupportedLanguage.ENGLISH : SupportedLanguage.valueOf(languageExtra);
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
                    askUserToTakePhoto();
                    openCamera();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textToSpeechReady) {
            askUserToTakePhoto();
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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Take a picture when the user push the select button
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (!currentlyTakingPhoto) {
                currentlyTakingPhoto = true;

                // Auto focus and take a photo
                textToSpeech.speak(getResources().getString(R.string.auto_focus), TextToSpeech.QUEUE_ADD, null);
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                currentlyTakingPhoto = false;
                                textToSpeech.speak(getResources().getString(R.string.photo_taken), TextToSpeech.QUEUE_ADD, null);

                                // Start analyzing the photo
                                Intent intent = new Intent(CameraActivity.this, PhotoAnalysisActivity.class);
                                intent.putExtra(SupportedLanguage.class.getSimpleName(), language.name());
                                intent.putExtra(PhotoAnalysisActivity.INTENT_PHOTO_EXTRA, data);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void askUserToTakePhoto() {
        textToSpeech.speak(getResources().getString(R.string.take_photo_explanation), TextToSpeech.QUEUE_ADD, null);
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
