package uy.gub.agesic.pdi.common.utiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.soap.DataUtil;

import java.io.UnsupportedEncodingException;

public class CanonicalProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CanonicalProcessor.class);

    public static Canonical<SoapPayload> createSoapCanonical(byte[] data) {

        Canonical<SoapPayload> canonical = new Canonical<>();
        SoapPayload soapPayload = new SoapPayload();
        if (data != null) {
            String dataEncoded = DataUtil.encode(data);
            soapPayload.setBase64Data(dataEncoded);
        }
        canonical.setPayload(soapPayload);
        canonical.getHeaders().put("type", "soap");
        return canonical;
    }

    public static byte[] getData(Canonical<SoapPayload> canonical) {
        SoapPayload soapPayload = canonical.getPayload();

        String dataEncoded = soapPayload.getBase64Data();
        if (dataEncoded != null) {
            return DataUtil.decode(dataEncoded);
        }
        return null;
    }

    public static String encodeData(String data, String cs) {
        if (data != null) {
            try {
                byte[] bytes = data.getBytes(cs);
                return DataUtil.encode(bytes);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static String decodeSoap(SoapPayload payload) throws Exception {
        String data = payload.getBase64Data();
        if (data == null) {
            return "";
        }

        byte[] bytes = DataUtil.decode(data);
        return new String(bytes, getCharSet(payload));
    }

    public static String getCharSet(Canonical<SoapPayload> message) {
        return getCharSet(message.getPayload());
    }

    private static String getCharSet(SoapPayload payload) {
        String contentType = payload.getContentType();
        return HttpUtil.getCharsetFromContentType(contentType);
    }

}
