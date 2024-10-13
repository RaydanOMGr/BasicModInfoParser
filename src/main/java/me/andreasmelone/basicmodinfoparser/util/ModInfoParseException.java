package me.andreasmelone.basicmodinfoparser.util;

/**
 * This exception indicates that parsing the mod info has failed
 */
public class ModInfoParseException extends RuntimeException {
    public ModInfoParseException(String message, Exception parentException) {
        super(message, parentException);
    }

    public ModInfoParseException(Exception parentException) {
        super(parentException);
    }
}
