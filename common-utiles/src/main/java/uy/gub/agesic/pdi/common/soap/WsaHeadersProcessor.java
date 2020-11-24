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

public class WsaHeadersProcessor {

    private static final String ACTION_HEADER_NAME = "wsaAction";

    private static final String MESSAGEID_HEADER_NAME = "wsaMessageID";

    private static final String TO_HEADER_NAME = "wsaTo";

    private static final String RELATESTO_HEADER_NAME = "wsaRelatesTo";

    private static final String REPLAYTO_HEADER_NAME = "wsaReplyTo";

    private static final String CORRELATIONID_HEADER_NAME = "correlationId";

    private static Map<String, String> namespaces = new HashMap<>();

    private static List<String> xpaths = new LinkedList<>();

    static {
        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:ReplyTo/wsa:Address/text()");
        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:MessageID/text()");
        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:To/text()");
        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:Action/text()");
        xpaths.add("/soapenv:Envelope/soapenv:Header/wsa:RelatesTo/text()");

        namespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");
        namespaces.put("wsa", "http://www.w3.org/2005/08/addressing");
    }


    public static void processHeaders(Canonical<SoapPayload> message, String xml) throws Exception {
        SoapPayload payload = message.getPayload();

        List<Object> listResultsXPath = XPathXmlDogUtil.executeMultipleXPath(xml, xpaths, namespaces);

        String wsaReplyTo;
        String wsaMessageID;
        String wsaTo;
        String wsaAction;

        if ((listResultsXPath.get(0) instanceof Collection)) {
            wsaReplyTo = (String) ((List) listResultsXPath.get(0)).get(0);
        } else {
            wsaReplyTo = (String) listResultsXPath.get(0);
        }
        payload.getWsaHeaders().put(REPLAYTO_HEADER_NAME, wsaReplyTo);

        if ((listResultsXPath.get(1) instanceof Collection)) {
            wsaMessageID = (String) ((List) listResultsXPath.get(1)).get(0);
        } else {
            wsaMessageID = (String) listResultsXPath.get(1);
        }
        payload.getWsaHeaders().put(MESSAGEID_HEADER_NAME, wsaMessageID);
        message.getHeaders().put(CORRELATIONID_HEADER_NAME, wsaMessageID);

        if ((listResultsXPath.get(2) instanceof Collection)) {
            wsaTo = (String) ((List) listResultsXPath.get(2)).get(0);
        } else {
            wsaTo = (String) listResultsXPath.get(2);
        }
        payload.getWsaHeaders().put(TO_HEADER_NAME, wsaTo);

        if ((listResultsXPath.get(3) instanceof Collection)) {
            wsaAction = (String) ((List) listResultsXPath.get(3)).get(0);
        } else {
            wsaAction = (String) listResultsXPath.get(3);
        }
        payload.getWsaHeaders().put(ACTION_HEADER_NAME, wsaAction);

        if ((listResultsXPath.get(4) instanceof Collection)) {
            List<String> wsaRelatesToList = (List) listResultsXPath.get(4);
            String[] wsaRelatesTo = (String[]) Arrays.copyOf(wsaRelatesToList.toArray(), wsaRelatesToList.size(), String[].class);
            StringBuilder wsaRelatesToAsString = new StringBuilder();
            int j = wsaRelatesTo.length;
            for (int i = 0; i < j; i++) {
                String relatesTo = wsaRelatesTo[i];
                if (i != 0) {
                    wsaRelatesToAsString.append(",");
                }
                wsaRelatesToAsString.append(relatesTo);
            }
            payload.getWsaHeaders().put(RELATESTO_HEADER_NAME, wsaRelatesToAsString.toString());
        } else if (listResultsXPath.get(4) != null) {
            payload.getWsaHeaders().put(RELATESTO_HEADER_NAME,(String) listResultsXPath.get(4));
        }
    }

    public static String getWsaAction(Canonical<SoapPayload> message) {
        return message.getPayload().getWsaHeaders().get(ACTION_HEADER_NAME);
    }

    public static String getWsaTo(Canonical<SoapPayload> message) {
        return message.getPayload().getWsaHeaders().get(TO_HEADER_NAME);
    }

    public static String getWsaMessageID(Canonical<SoapPayload> message) {
        return message.getPayload().getWsaHeaders().get(MESSAGEID_HEADER_NAME);
    }

    public static String getWsaRelatesTo(Canonical<SoapPayload> message) {
        return message.getPayload().getWsaHeaders().get(RELATESTO_HEADER_NAME);
    }

}
