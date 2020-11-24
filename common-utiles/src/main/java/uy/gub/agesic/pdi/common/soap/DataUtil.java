package uy.gub.agesic.pdi.common.soap;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class DataUtil {

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static String decodeToString(String data) throws UnsupportedEncodingException {
        return decodeToString(data, null);
    }

    public static String decodeToString(String data, String contentType) throws UnsupportedEncodingException {
        if (contentType == null) {
            contentType = "UTF-8";
        }
        return new String(decode(data), contentType);
    }
}
