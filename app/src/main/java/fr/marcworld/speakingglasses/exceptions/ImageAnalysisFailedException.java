package fr.marcworld.speakingglasses.exceptions;

import fr.marcworld.speakingglasses.enums.ImageAnalysisStatus;

/**
 * Exception thrown when an image analysis failed.
 *
 * @author Marc Plouhinec
 */
public class ImageAnalysisFailedException extends Exception {

    private final ImageAnalysisStatus imageAnalysisStatus;

    public ImageAnalysisFailedException(ImageAnalysisStatus imageAnalysisStatus) {
        super("Image analysis failed: " + imageAnalysisStatus + ".");
        this.imageAnalysisStatus = imageAnalysisStatus;
    }

    public ImageAnalysisFailedException(String detailMessage, ImageAnalysisStatus imageAnalysisStatus) {
        super("Image analysis failed: " + imageAnalysisStatus + ". " + detailMessage);
        this.imageAnalysisStatus = imageAnalysisStatus;
    }

    public ImageAnalysisFailedException(String detailMessage, Throwable throwable, ImageAnalysisStatus imageAnalysisStatus) {
        super("Image analysis failed: " + imageAnalysisStatus + ". " + detailMessage, throwable);
        this.imageAnalysisStatus = imageAnalysisStatus;
    }

    public ImageAnalysisStatus getImageAnalysisStatus() {
        return imageAnalysisStatus;
    }
}
