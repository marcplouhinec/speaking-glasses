package fr.marcworld.speakingglasses.services;

import fr.marcworld.speakingglasses.enums.SupportedLanguage;
import fr.marcworld.speakingglasses.exceptions.ImageAnalysisFailedException;
import fr.marcworld.speakingglasses.exceptions.NoWebConnectionException;

/**
 * Give access to the API of https://cloudsight.ai/.
 *
 * @author Marc Plouhinec
 */
public interface CloudSightService {

    /**
     * Send a request to the Cloud Sight API for analyzing the given image, then wait and return the result.
     * Note that this method should be called in a background thread.
     *
     * @param image    Image data.
     * @param language Language to use for describing the image.
     * @return Sentence describing the image.
     */
    String analyzeImage(byte[] image, SupportedLanguage language) throws NoWebConnectionException, ImageAnalysisFailedException;

}
