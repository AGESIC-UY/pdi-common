package uy.gub.agesic.pdi.common.utiles;

import java.util.HashMap;
import java.util.Map;

public class XSLTInformation {

    private String name;

    private String path;

    private Map<String, String> parameters = new HashMap<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public String toString() {
        return "XSLTInformation: " + this.name + " - Path: " + this.path + " (" + this.parameters + ")";
    }
}

