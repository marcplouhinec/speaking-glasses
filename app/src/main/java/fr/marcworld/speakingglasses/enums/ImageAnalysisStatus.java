package fr.marcworld.speakingglasses.enums;

/**
 * Status of the analysis of an image.
 *
 * @author Marc Plouhinec
 */
public enum ImageAnalysisStatus {
    NOT_COMPLETED,
    COMPLETED,
    NOT_FOUND,
    SKIPPED,
    TIMEOUT,
    UNKNOWN;

    /**
     * @return true if there is no need to wait anymore, false if the image analysis is not yet finished.
     */
    public boolean isFinal() {
        return this != NOT_COMPLETED;
    }
}
