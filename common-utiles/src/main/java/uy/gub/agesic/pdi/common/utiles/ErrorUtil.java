package uy.gub.agesic.pdi.common.utiles;

import uy.gub.agesic.pdi.common.message.canonical.Error;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtil {

    public static Error createError(Exception e) {
        return createError((Throwable) e);
    }

    public static Error createError(Throwable t) {
        Error error = new Error();
        error.setMessage(t.getMessage());

        StringWriter errors = new StringWriter();
        t.printStackTrace(new PrintWriter(errors));
        String description = errors.toString();

        error.setDescription(description);
        error.setCode("Internal Error");
        return error;
    }

}
