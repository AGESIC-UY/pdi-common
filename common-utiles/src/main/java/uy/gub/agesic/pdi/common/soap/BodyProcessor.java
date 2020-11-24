package uy.gub.agesic.pdi.common.soap;

import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.utiles.XPathXmlDogUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BodyProcessor {

    private static final String EMPTY_BODY_HEADER_NAME = "emptyBody";

    private static Map<String, String> namespaces = new HashMap<>();

    private static List<String> xpaths = new LinkedList<>();

    static {
        xpaths.add("count(/soapenv:Envelope/soapenv:Body/*[1]) = 0");

        namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");
    }

    public static void processSoap(Canonical<SoapPayload> message, String xml) throws Exception {
        List<Object> listResultsXPath = XPathXmlDogUtil.executeMultipleXPath(xml, xpaths, namespaces);

        Boolean emptyBody;
        if ((listResultsXPath.get(0) instanceof Collection)) {
            emptyBody = (Boolean) ((List) listResultsXPath.get(0)).get(0);
        } else {
            emptyBody = (Boolean) listResultsXPath.get(0);
        }
        message.getHeaders().put(EMPTY_BODY_HEADER_NAME, Boolean.valueOf(emptyBody == null || emptyBody.booleanValue()));
    }

    public static boolean isEmptyBody(Canonical<SoapPayload> message) {
        return (Boolean) message.getHeaders().get(EMPTY_BODY_HEADER_NAME);
    }

}
