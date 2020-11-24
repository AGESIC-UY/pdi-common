package uy.gub.agesic.pdi.common.message.canonical;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Canonical<T> implements Serializable {

    private Map<String, Object> headers;

    private T payload;

    public Canonical() {
        this.headers = new HashMap<String, Object>();
        this.payload = null;
    }

    public Canonical(T payload) {
        this.headers = new HashMap<String, Object>();
        this.payload = payload;
    }

    public Canonical(Map<String, Object> headers, T payload) {
        this.headers = headers;
        if (this.headers == null) {
            this.headers = new HashMap<String, Object>();
        }
        this.payload = payload;
    }

    public Map<String, Object> getHeaders() {
        if (headers == null) {
            headers = new HashMap<String, Object>();
        }
        return headers;
    }

    public Canonical<T> setHeaders(Map<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    public T getPayload() {
        return payload;
    }

    public Canonical<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("{\n");
        strBuilder.append("\t\"headers\": {\n");

        if (headers != null) {
            Set<String> keys = headers.keySet();
            for (String key : keys) {
                Object value = headers.get(key);
                strBuilder.append("\t\t\"");
                strBuilder.append(key);
                strBuilder.append("\": ");

                strBuilder.append("\"");
                strBuilder.append(value);
                strBuilder.append("\",\n");
            }
        }

        strBuilder.append("\t},\n");

        strBuilder.append("{\n");
        strBuilder.append("\t\"payload\": {\n");
        strBuilder.append("\t\t");
        if (payload != null) {
            strBuilder.append(payload.toString());
        }
        strBuilder.append("\t}\n");
        strBuilder.append("}");

        return strBuilder.toString();
    }

}
