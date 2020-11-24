package uy.gub.agesic.pdi.common.message.soap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SoapPayload implements Serializable {

    private String contentType;

    private Map<String, String> wsaHeaders = new HashMap<String, String>();

    private String base64Data;

    private String responseStatusCode;

    private byte[] dataMTOM;

    private String mtomContentType;

    public String getContentType() {
        return contentType;
    }

    public SoapPayload setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Map<String, String> getWsaHeaders() {
        return wsaHeaders;
    }

    public SoapPayload setWsaHeaders(Map<String, String> wsaHeaders) {
        this.wsaHeaders = wsaHeaders;
        return this;
    }

    public String getBase64Data() {
        return base64Data;
    }

    public SoapPayload setBase64Data(String base64Data) {
        this.base64Data = base64Data;
        return this;
    }

    public String getResponseStatusCode() {
        return responseStatusCode;
    }

    public SoapPayload setResponseStatusCode(String responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
        return this;
    }

    public byte[] getDataMTOM() {
        return dataMTOM;
    }

    public SoapPayload setDataMTOM(byte[] dataMTOM) {
        this.dataMTOM = dataMTOM;
        return this;
    }

    public String getMtomContentType() {
        return mtomContentType;
    }

    public SoapPayload setMtomContentType(String mtomContentType) {
        this.mtomContentType = mtomContentType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\t\t\"wsa\": {\n");

        if (wsaHeaders != null) {
            Set<String> keys = wsaHeaders.keySet();
            for (String key: keys) {
                Object value = wsaHeaders.get(key);
                strBuilder.append("\t\t\"");
                strBuilder.append(key);
                strBuilder.append("\": ");

                strBuilder.append("\"");
                strBuilder.append(value);
                strBuilder.append("\",\n");
            }
        }

        strBuilder.append("\t\t},\n");

        strBuilder.append("\t\t\"contentType\": \n");
        strBuilder.append("\"");
        strBuilder.append(contentType);
        strBuilder.append("\",\n");
/*
        strBuilder.append("\t\t\"base64Data\": \n");
        strBuilder.append("\"");
        strBuilder.append(base64Data);
        strBuilder.append("\",\n");
*/
        strBuilder.append("\t\t\"responseStatusCode\": \n");
        strBuilder.append("\"");
        strBuilder.append(responseStatusCode);
        strBuilder.append("\",\n");

        return strBuilder.toString();
    }

}
