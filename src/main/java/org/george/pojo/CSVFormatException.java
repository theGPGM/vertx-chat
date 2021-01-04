package org.george.pojo;

public class CSVFormatException extends RuntimeException {

    public CSVFormatException() {
        super();
    }

    public CSVFormatException(String message) {
        super(message);
    }

    public CSVFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public CSVFormatException(Throwable cause) {
        super(cause);
    }

    protected CSVFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
