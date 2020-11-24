package uy.gub.agesic.pdi.services.httpproxy.business;

import java.io.Serializable;

public class PropertyInfo implements Serializable {

    private String code;

    private String description;

    private String message;

    public String getCode() {
        return code;
    }

    public PropertyInfo () {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
