package uy.gub.agesic.pdi.common.exceptions;

public class PDIException extends Exception {

    public PDIException() {}

    public PDIException(String message) {
        super(message);
    }

    public PDIException(String message, Throwable cause) {
        super(message, cause);
    }

    public PDIException(Throwable cause) {
        super(cause);
    }

    public PDIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
