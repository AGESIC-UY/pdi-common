package uy.gub.agesic.pdi.common.utiles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtil {

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static String getCharsetFromContentType(String contentType) {
        Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

        String csn = DEFAULT_CHARSET;
        if (contentType == null) {
            return csn;
        }

        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            csn = m.group(1).trim().toUpperCase();
            if (!Charset.isSupported(csn)) {
                csn = DEFAULT_CHARSET;
            }
        }

        return csn;
    }

    public static Map<String, String> getHeadersRequest (HttpServletRequest request){

        Map<String, String> map = new HashMap<String, String>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    public static Map<String, String> getHeadersResponse(HttpServletResponse response){

       Map<String,String> map = new HashMap<String, String>();
          Collection headerNames =  response.getHeaderNames();
          Iterator it = headerNames.iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            String value = response.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

}
