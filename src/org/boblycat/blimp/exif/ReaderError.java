package org.boblycat.blimp.exif;

/**
 * Exif reader error.
 * 
 * @author Knut Arild Erstad
 */
public class ReaderError extends Exception {
    private static final long serialVersionUID = 1L;

    public ReaderError(String message) {
        super(message);
    }
    
    public ReaderError(String message, Throwable cause) {
        super(message, cause);
    }
}
