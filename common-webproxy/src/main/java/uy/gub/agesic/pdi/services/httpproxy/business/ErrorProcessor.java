package uy.gub.agesic.pdi.services.httpproxy.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uy.gub.agesic.pdi.common.message.canonical.Error;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class ErrorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ErrorProcessor.class);

    private ResourceResolver rr;

    private Map<String, PropertyInfo> errors;

    private static final String ERRORS_JSON = "errors.json";

    @Autowired
    public ErrorProcessor(ResourceResolver resourceResolver) {
        this.rr = resourceResolver;
    }

    @PostConstruct
    public void init() {
        try {
            errors = rr.getProperties(ERRORS_JSON);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Error createError(String message, String code, String description) {
        Error error = new Error();
        error.setMessage(message);
        error.setCode(code);
        String desc = String.format("%s - %s", code, getDescriptionError(error));
        error.setCode("env:Server");
        error.setDescription(desc != null ? desc : description);
        return error;
    }

    public String getDescriptionError(Error error) {
        return getDescriptionByCode(error.getCode());
    }

    public String getDescriptionByCode(String code) {
        if (errors == null) {
            init();
        }
        return (errors != null && errors.get(code) != null) ? errors.get(code).getMessage() : null;
    }

}

