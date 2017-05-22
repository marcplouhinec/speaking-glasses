package fr.marcworld.speakingglasses.services.impl;

import com.reconinstruments.os.HUDOS;
import com.reconinstruments.os.connectivity.HUDConnectivityManager;
import com.reconinstruments.os.connectivity.http.HUDHttpRequest;
import com.reconinstruments.os.connectivity.http.HUDHttpResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.marcworld.speakingglasses.enums.ImageAnalysisStatus;
import fr.marcworld.speakingglasses.enums.SupportedLanguage;
import fr.marcworld.speakingglasses.exceptions.ImageAnalysisFailedException;
import fr.marcworld.speakingglasses.exceptions.NoWebConnectionException;
import fr.marcworld.speakingglasses.services.CloudSightService;

/**
 * Default implementation of {@link CloudSightService}.
 *
 * @author Marc Plouhinec
 */
public class CloudSightServiceImpl implements CloudSightService {

    private final String cloudSightApiKey;
    private final HUDConnectivityManager connectivityManager;

    public CloudSightServiceImpl(String cloudSightApiKey) {
        this.cloudSightApiKey = cloudSightApiKey;
        this.connectivityManager = (HUDConnectivityManager) HUDOS.getHUDService(HUDOS.HUD_CONNECTIVITY_SERVICE);
    }

    @Override
    public String analyzeImage(byte[] image, SupportedLanguage language) throws NoWebConnectionException, ImageAnalysisFailedException {
        if (!connectivityManager.hasWebConnection()) {
            throw new NoWebConnectionException();
        }

        // Build and send a HTTP POST request in order to request an image analysis
        String requestToken = null;
        try {
            // Prepare the body
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            outStream.write(("--------------------------5582c9e66e294183\r\n" +
                    "Content-Disposition: form-data; name=\"image_request[image]\"; filename=\"photo.jpeg\"\r\n" +
                    "Content-Type: image/jpeg\r\n" +
                    "\r\n").getBytes());
            outStream.write(image);
            outStream.write(("\r\n--------------------------5582c9e66e294183\r\n" +
                    "Content-Disposition: form-data; name=\"image_request[locale]\"\r\n" +
                    "\r\n" +
                    language.getTag() + "\r\n" +
                    "--------------------------5582c9e66e294183\r\n" +
                    "Content-Disposition: form-data; name=\"image_request[language]\"\r\n" +
                    "\r\n" +
                    language.getIsoCode() + "\r\n" +
                    "--------------------------5582c9e66e294183--\r\n").getBytes());
            byte[] body = outStream.toByteArray();

            // Prepare the header
            final Map<String, List<String>> headers = new HashMap<>();
            headers.put("Authorization", Collections.singletonList("CloudSight " + cloudSightApiKey));
            headers.put("Accept", Collections.singletonList("*/*"));
            headers.put("Expect", Collections.singletonList("100-continue"));
            headers.put("Content-Type", Collections.singletonList("multipart/form-data; boundary=------------------------5582c9e66e294183"));
            headers.put("Content-Length", Collections.singletonList(String.valueOf(body.length)));

            // Send the request and wait for the response
            HUDHttpRequest httpRequest = new HUDHttpRequest(HUDHttpRequest.RequestMethod.POST, new URL("http://api.cloudsightapi.com/image_requests"), headers, body);
            HUDHttpResponse httpResponse = connectivityManager.sendWebRequest(httpRequest);
            if (httpResponse.getResponseCode() != 200) {
                throw new ImageAnalysisFailedException("Unable to send an image analysis request.", ImageAnalysisStatus.UNKNOWN);
            }
            JSONObject jsonObject = new JSONObject(httpResponse.getBodyString());
            requestToken = jsonObject.getString("token");
        } catch (Exception e) {
            throw new ImageAnalysisFailedException("Unable to send an image analysis request.", e, ImageAnalysisStatus.UNKNOWN);
        }

        // Regularly request the webservice for an image analysis response
        String imageDescription = null;
        ImageAnalysisStatus status = ImageAnalysisStatus.NOT_COMPLETED;
        try {
            // Prepare the header
            final Map<String, List<String>> headers = new HashMap<>();
            headers.put("Authorization", Collections.singletonList("CloudSight " + cloudSightApiKey));

            // Loop until the analysis is done
            while (!status.isFinal()) {
                // Wait for a while
                Thread.sleep(1000);

                // Query the webservice for an update
                HUDHttpRequest httpRequest = new HUDHttpRequest(HUDHttpRequest.RequestMethod.GET, new URL("http://api.cloudsightapi.com/image_responses/" + requestToken), headers);
                HUDHttpResponse httpResponse = connectivityManager.sendWebRequest(httpRequest);

                if (httpResponse.getResponseCode() == 200 || httpResponse.getResponseCode() == 404) {
                    JSONObject jsonObject = new JSONObject(httpResponse.getBodyString());
                    status = parseImageResponseStatus(jsonObject.getString("status"));
                    if (jsonObject.has("name")) {
                        imageDescription = jsonObject.getString("name");
                    }
                }
            }
        } catch (Exception e) {
            throw new ImageAnalysisFailedException("Unable to receive an image analysis response.", e, ImageAnalysisStatus.UNKNOWN);
        }

        // Throw an exception if the status is not right
        if (status != ImageAnalysisStatus.COMPLETED) {
            throw new ImageAnalysisFailedException(status);
        }

        return imageDescription;
    }

    private ImageAnalysisStatus parseImageResponseStatus(String responseStatus) {
        if (responseStatus == null) {
            return ImageAnalysisStatus.UNKNOWN;
        }

        switch (responseStatus.trim().toLowerCase()) {
            case "not completed":
                return ImageAnalysisStatus.NOT_COMPLETED;
            case "completed":
                return ImageAnalysisStatus.COMPLETED;
            case "not found":
                return ImageAnalysisStatus.NOT_FOUND;
            case "skipped":
                return ImageAnalysisStatus.SKIPPED;
            case "timeout":
                return ImageAnalysisStatus.TIMEOUT;
            default:
                return ImageAnalysisStatus.UNKNOWN;
        }
    }
}
