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

import com.reconinstruments.os.HUDOS;
import com.reconinstruments.os.connectivity.HUDConnectivityManager;
import com.reconinstruments.os.connectivity.http.HUDHttpRequest;
import com.reconinstruments.os.connectivity.http.HUDHttpResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import fr.marcworld.speakingglasses.enums.SupportedLanguage;
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
    private HUDConnectivityManager connectivityManager;
    private final Properties applicationProperties = new Properties();
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
                    startPhotoAnalysis(photo, language);
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

    private void startPhotoAnalysis(byte[] photo, SupportedLanguage language) {
        // Check we have internet connectivity
        if (!connectivityManager.hasWebConnection()) {
            Toast.makeText(PhotoAnalysisActivity.this, R.string.error_no_connectivity, Toast.LENGTH_SHORT).show();
            textToSpeech.speak(getResources().getString(R.string.error_no_connectivity), TextToSpeech.QUEUE_ADD, null);
            return;
        }

        // Send the photo to Cloud Sight API to analyze the photo
        Toast.makeText(PhotoAnalysisActivity.this, R.string.analyze_photo, Toast.LENGTH_SHORT).show();
        textToSpeech.speak(getResources().getString(R.string.analyze_photo), TextToSpeech.QUEUE_ADD, null);

        String cloudSightApiKey = applicationProperties.getProperty("cloudSight.apiKey");
        new UploadPhotoTask(photo, cloudSightApiKey, language).execute();
    }

    // TODO externalize that in a service
    private class UploadPhotoTask extends AsyncTask<String, Void, Boolean> {

        private final byte[] photo;
        private final String cloudSightApiKey;
        private final SupportedLanguage language;

        public UploadPhotoTask(byte[] photo, String cloudSightApiKey, SupportedLanguage language) {
            this.photo = photo;
            this.cloudSightApiKey = cloudSightApiKey;
            this.language = language;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Map<String, List<String>> headers = new HashMap<>();
                headers.put("Authorization", Collections.singletonList("CloudSight " + cloudSightApiKey));
                headers.put("Accept", Collections.singletonList("*/*"));
                headers.put("Expect", Collections.singletonList("100-continue"));
                headers.put("Content-Type", Collections.singletonList("multipart/form-data; boundary=------------------------5582c9e66e294183"));
                String contentStart = "--------------------------5582c9e66e294183\r\n" +
                        "Content-Disposition: form-data; name=\"image_request[image]\"; filename=\"photo.jpeg\"\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "\r\n";
                String contentEnd = "\r\n--------------------------5582c9e66e294183\r\n" +
                        "Content-Disposition: form-data; name=\"image_request[locale]\"\r\n" +
                        "\r\n" +
                        language.getTag() + "\r\n" +
                        "--------------------------5582c9e66e294183\r\n" +
                        "Content-Disposition: form-data; name=\"image_request[language]\"\r\n" +
                        "\r\n" +
                        language.getIsoCode() + "\r\n" +
                        "--------------------------5582c9e66e294183--\r\n";
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                outStream.write(contentStart.getBytes());
                outStream.write(photo);
                outStream.write(contentEnd.getBytes());
                byte[] body = outStream.toByteArray();
                headers.put("Content-Length", Collections.singletonList(String.valueOf(body.length)));

                HUDHttpRequest postRequest = new HUDHttpRequest(HUDHttpRequest.RequestMethod.POST, new URL("http://api.cloudsightapi.com/image_requests"), headers, body);
                HUDHttpResponse postResponse = connectivityManager.sendWebRequest(postRequest);
                if (postResponse.getResponseCode() != 200) {
                    // TODO
                    return null;
                }

                JSONObject jsonObject = new JSONObject(postResponse.getBodyString());
                String token = jsonObject.getString("token");

                headers = new HashMap<>();
                headers.put("Authorization", Collections.singletonList("CloudSight " + cloudSightApiKey));

                String status = "not completed"; // TODO put this value in an enum
                String name = null;
                while (!status.equalsIgnoreCase("completed") && !status.equalsIgnoreCase("timeout") && !status.equalsIgnoreCase("skipped") && !status.equalsIgnoreCase("not found")) {
                    Thread.sleep(1000);
                    HUDHttpRequest getRequest = new HUDHttpRequest(HUDHttpRequest.RequestMethod.GET, new URL("http://api.cloudsightapi.com/image_responses/" + token), headers);
                    HUDHttpResponse getResponse = connectivityManager.sendWebRequest(getRequest);

                    if (getResponse.getResponseCode() == 200 || getResponse.getResponseCode() == 404) {
                        jsonObject = new JSONObject(getResponse.getBodyString());
                        status = jsonObject.getString("status");
                        if (jsonObject.has("name")) {
                            name = jsonObject.getString("name");
                        }
                    }
                }

                // TODO clean the code
                if (name != null) {
                    final String name2 = name;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(PhotoAnalysisActivity.this, name2, Toast.LENGTH_LONG).show();
                            textToSpeech.speak(name2, TextToSpeech.QUEUE_ADD, null);
                        }
                    });
                }

            } catch (Exception e) {
                // TODO
            }

            return null;
        }
    }
}
