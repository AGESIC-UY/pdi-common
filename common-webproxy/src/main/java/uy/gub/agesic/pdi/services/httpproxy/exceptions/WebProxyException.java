package uy.gub.agesic.pdi.services.httpproxy.exceptions;


import java.io.PrintWriter;
import java.io.StringWriter;

public class WebProxyException extends Exception {

    private String message;

    private String description;

    private String code;

    public WebProxyException(String message, String description) {
        code = "Internal Error";
        this.message = message;
        this.description = description;
    }

    public WebProxyException(String message, String description, String code, Throwable t) {
        super(t);
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public WebProxyException(Throwable t) {
        if (t instanceof WebProxyException) {
            WebProxyException wrException = (WebProxyException) t;
            this.code = wrException.getCode();
            this.description = wrException.getDescription();
            this.message = wrException.getMessage();
        } else {
            code = "Internal Error";
            message = t.getMessage();

            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            description = errors.toString();
        }
    }

    public WebProxyException() {
    }

    public WebProxyException(String message) {
        super(message);
    }

    public WebProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebProxyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }


}
