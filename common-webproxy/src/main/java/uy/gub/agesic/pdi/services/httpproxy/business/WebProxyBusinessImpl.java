package uy.gub.agesic.pdi.services.httpproxy.business;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import uy.gub.agesic.pdi.common.logging.Loggable;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.canonical.Error;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.common.soap.WsaHeadersProcessor;
import uy.gub.agesic.pdi.common.utiles.ErrorUtil;
import uy.gub.agesic.pdi.common.soap.DataUtil;
import uy.gub.agesic.pdi.services.httpproxy.exceptions.WebProxyException;
import uy.gub.agesic.pdi.services.httpproxy.util.Constants;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;

@Service("WebProxyService")
@RefreshScope
public class WebProxyBusinessImpl implements WebProxyBusiness {

    private static final Logger logger = LoggerFactory.getLogger(WebProxyBusinessImpl.class);

    private static final HashMap<String, HttpClientBuilder> cmMap = new HashMap<>();

    private ResourceResolver rr;

    private ErrorProcessor errorProcessor;

    @Value("${keystore.password:password}")
    private String ksPassword;

    @Value("${keystore.name:kestore.jks}")
    private String ksName;

    @Value("${trustore.password:password}")
    private String tsPassword;

    @Value("${trustore.name:trustore.jks}")
    private String tsName;

    @Value("${http.pool.maxPerRoute:10}")
    private int maxPerRoute = 10;

    @Value("${http.defaultTimeout:10}")
    private int defaultTimeout = 10;

    @Autowired
    public WebProxyBusinessImpl (ResourceResolver rr, ErrorProcessor errorProcessor) {
        this.rr = rr;
        this.errorProcessor = errorProcessor;
    }

    @Loggable
    public Canonical<SoapPayload> invokeEndpoint(Canonical<SoapPayload> message) throws WebProxyException {
        long start = System.currentTimeMillis();

        Map<String, Object> headers = message.getHeaders();
        String payloadType = (String) headers.get("type");
        if (payloadType.equalsIgnoreCase("soap")) {
            message = invokeWS(message);

            message.getHeaders().put("webProxyTimestamp", System.currentTimeMillis() - start);
            return message;
        } else {
            throw new WebProxyException("Error, formato request inv\u00E1lido" , null, Constants.ERRORINVALIDSOAP, null);
        }
    }

    private Canonical<SoapPayload> invokeEndpointFallback(Canonical<SoapPayload> message, Throwable e) {
        logger.error(e.getMessage(), e);
        return processError(e);
    }

    private Canonical<SoapPayload> invokeWS(Canonical<SoapPayload> message) {
        SoapPayload payload = message.getPayload();

        Map<String, Object> headers = message.getHeaders();
        String endpoint = (String) headers.get("url");

        String soapAction = WsaHeadersProcessor.getWsaAction(message);

        HttpPost post = null;
        try {
            int timeout = headers.get("timeout") != null ? (int) headers.get("timeout") : defaultTimeout;
            CloseableHttpClient httpClient = createHttpClient(endpoint, timeout);

            HttpEntity entity = getEntity(payload.getBase64Data());

            headers = new HashMap<>();
            headers.put("SOAPAction", soapAction != null && !soapAction.startsWith("\"") ? "\"" + soapAction + "\"": soapAction);
            headers.put("Content-Type", payload.getContentType());

            post = new HttpPost(endpoint);

            long start = System.currentTimeMillis();

            post.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);

            if (logger.isTraceEnabled()) {
                logger.trace("Request payload - " + new String(DataUtil.decode(payload.getBase64Data())));
            }
            CloseableHttpResponse response = invokeHttp(headers, post, httpClient, entity);
            int result = response.getStatusLine().getStatusCode();

            InputStream is = response.getEntity().getContent();
            byte[] bytes = IOUtils.toByteArray(is);
            long end = System.currentTimeMillis();

            if (logger.isTraceEnabled()) {
                logger.trace("Response payload - " + new String(bytes));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Response headers - ");
                for (Header header : response.getAllHeaders()) {
                    stringBuilder.append(header.getName());
                    stringBuilder.append(" - ");
                    stringBuilder.append(header.getValue());
                    stringBuilder.append(" | ");
                }
                logger.trace(stringBuilder.toString());
            }

            Canonical<SoapPayload> cResponse = new Canonical<>();
            payload = new SoapPayload();
            payload.setResponseStatusCode("" + result);
            cResponse.setPayload(payload);
            cResponse.getHeaders().put("serviceTimestamp", Long.toString(end - start));

            if (bytes != null && bytes.length > 0) {
                payload.setBase64Data(DataUtil.encode(bytes));
                payload.setContentType(response.getEntity().getContentType().getValue());
                payload.setResponseStatusCode(String.valueOf(result));
            }

            if ((result != 200) && (result != 202) && (result != 204)) {
                for (Header header : response.getAllHeaders()) {
                    cResponse.getHeaders().put(header.getName(), header.getValue());
                }
            }

            return cResponse;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return processError(e);
        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
    }

    private Canonical<SoapPayload> processError(Throwable t) {
        Canonical<SoapPayload> cResponse = new Canonical<>();

        Error error = null;
        if (t instanceof WebProxyException) {
            WebProxyException wpe = (WebProxyException) t;
            error = errorProcessor.createError(wpe.getMessage(),wpe.getCode(),wpe.getDescription());
        } else {
            error = ErrorUtil.createError(t);
        }
        cResponse.getHeaders().put("error", error);

        SoapPayload payload = new SoapPayload();
        payload.setResponseStatusCode("500");
        cResponse.setPayload(payload);
        return cResponse;
    }

    private CloseableHttpClient createHttpClient(String endpoint, int timeout) throws Exception {

        HttpClientBuilder clientBuilder = null;

        synchronized (cmMap) {
            if (cmMap.get(endpoint) == null) {
                clientBuilder = HttpClientBuilder.create();
                cmMap.put(endpoint, clientBuilder);
            } else {
                return cmMap.get(endpoint).build();
            }
        }

        PoolingHttpClientConnectionManager cm = null;

        if (endpoint.startsWith("https")) {
            LayeredConnectionSocketFactory sslsf = createSSLConfig();
            clientBuilder.setSSLSocketFactory(sslsf);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslsf).build();

            cm = new PoolingHttpClientConnectionManager(registry);
        } else {
            cm = new PoolingHttpClientConnectionManager();
        }

        cm.setDefaultMaxPerRoute(maxPerRoute);
        clientBuilder.setConnectionManager(cm);

        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        clientBuilder.setDefaultRequestConfig(config);

        return clientBuilder.build();
    }

    private LayeredConnectionSocketFactory createSSLConfig() throws Exception {

        try {
            ResourceResolver.KeystoreInfo keyStoreInfo = rr.getKeystore(ksName, ksPassword);
            KeyStore keyStore = keyStoreInfo.getKeystore();

            ResourceResolver.KeystoreInfo trustStoreInfo = rr.getKeystore(tsName, tsPassword);
            KeyStore trustStore = trustStoreInfo.getKeystore();

            if (trustStore == null) {
                WebProxyException e = new WebProxyException("Error, trustore no encontrado", null, Constants.ERRORTRUSTORE, null);
                throw e;
            }

            //Se valida la vigencia del certificado, si está vencido se lanza una excepción
            isExpiredCertificate(keyStore);

                SSLContext context = SSLContexts.custom().loadTrustMaterial(trustStore, null).
                    loadKeyMaterial(keyStore, keyStoreInfo.getPass().toCharArray()).build();
            return new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);

        } catch (WebProxyException wpe) {
            throw wpe;
        } catch (Exception e) {
            throw new WebProxyException("Error al obtener keystore y trustore", null, Constants.ERRORGETKTSTORE, e);
        }
    }



    private void isExpiredCertificate( KeyStore keyStore) throws Exception {
        String cn = null;
        Enumeration aliases = null;
        boolean expired = false;
        try {
            aliases = keyStore.aliases();

            for(; aliases.hasMoreElements();) {
                String alias = (String) aliases.nextElement();
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
                Date certExpiryDate = cert.getNotAfter();
                cn = cert.getSubjectX500Principal().getName() ;

                Date today = new Date();
                expired = certExpiryDate.getTime() < today.getTime();
            }

        } catch (Exception e) {
            throw new WebProxyException("Error al validar vigencia del certificado " + cn, null, Constants.ERRORVALIDATINGCERTIFICATE, e);
        }

        if (expired) {
            WebProxyException wpe = new WebProxyException("Error, el certificado " + cn + " ha expirado.", null, Constants.ERROREXPIREDCERTIFICATE, null);
            throw wpe;
        }

    }

    private CloseableHttpResponse invokeHttp(Map<String, Object> headers, HttpEntityEnclosingRequestBase httpRequest,
                                             CloseableHttpClient httpclient, HttpEntity entity) throws Exception {

        if (headers != null && !headers.isEmpty()) {
            for (String headerName : headers.keySet()) {
                httpRequest.setHeader(headerName, (String) headers.get(headerName));
            }
        }

        CloseableHttpResponse response;
        try {
            httpRequest.setEntity(entity);
            response = httpclient.execute(httpRequest);
        } catch (SocketTimeoutException te) {
            throw new WebProxyException("Error, timeout", null, Constants.ERRORTIMEOUT, te);
        } catch (ConnectException ce) {
            throw new WebProxyException("Error, conexi\u00F3n rehusada", null, Constants.ERRORCONNECTED, ce);
        } catch (UnknownHostException uhe) {
            throw new WebProxyException("Error, nombre o host desconocido", null, Constants.ERRORUNKNOWNHOST, uhe);
        } catch (IllegalArgumentException iae) {
            throw new WebProxyException("Error, host o puerto incorrecto", null, Constants.ERRORHOSTPORT, iae);
        } catch(SSLException ex){
            throw new WebProxyException("Error de conexi\u00F3n SSL", null, Constants.ERRORCONNECTIONSSL, ex);
        } catch (Exception e) {
            throw e;
        }

        return response;
    }

    private HttpEntity getEntity(String base64Data) {
        if (base64Data == null) {
            return null;
        }

        byte[] data = DataUtil.decode(base64Data);
        return new ByteArrayEntity(data);
    }

    public static void main(String[] args) throws Exception {
        String str = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soa=\"http://www.agesic.gub.uy/soa\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <soa:GetTimestamp/>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>>";

        System.out.println("SOAP :" + str);
        System.out.println("byteArray :" + str.getBytes());

        String strEncoded = DataUtil.encode(str.getBytes());

        System.out.println("Base64 :" + strEncoded);

        String encoded = "PFNPQVAtRU5WOkVudmVsb3BlIHhtbG5zOlNPQVAtRU5WPSJodHRwOi8vc2NoZW1hcy54bWxzb2FwLm9yZy9zb2FwL2VudmVsb3BlLyI+PFNPQVAtRU5WOkhlYWRlci8+PFNPQVAtRU5WOkJvZHk+PG5zMzpHZXRUaW1lc3RhbXBSZXNwb25zZSB4bWxuczpuczM9Imh0dHA6Ly93d3cuYWdlc2ljLmd1Yi51eS9zb2EiPjxUaW1lU3RhbXA+MjAxNy0wOS0xNFQxOTowNjoyMi41NTRaPC9UaW1lU3RhbXA+PC9uczM6R2V0VGltZXN0YW1wUmVzcG9uc2U+PC9TT0FQLUVOVjpCb2R5PjwvU09BUC1FTlY6RW52ZWxvcGU+";
        //encoded = strEncoded;
        String response = DataUtil.decodeToString(encoded);
        System.out.println("Response :" + response);

        String e2 = "eyJ0aW1lc3RhbXAiOjE1MDU4MzQwMjMzNzMsInN0YXR1cyI6NDA1LCJlcnJvciI6Ik1ldGhvZCBOb3QgQWxsb3dlZCIsIm1lc3NhZ2UiOiJIVFRQIG1ldGhvZCBQT1NUIGlzIG5vdCBzdXBwb3J0ZWQgYnkgdGhpcyBVUkwiLCJwYXRoIjoiL3JvdXRlclNlcnZpY2Uvcm91dGUifQ==";
        System.out.println("Response2 :" + DataUtil.decodeToString(e2));

        String str2 = "http://ibm.com/Service/Ping";
        System.out.println("str2 = [" + DataUtil.encode(str2.getBytes("UTF-8")) + "]");

        str2 = "http:///www.agesic.gub.uy/Service/Echo";
        System.out.println("str3 = [" + DataUtil.encode(str2.getBytes("UTF-8")) + "]");

    }


    private String getErrorByCode(String code){
        return code + " " + errorProcessor.getDescriptionByCode(code);
    }

}
