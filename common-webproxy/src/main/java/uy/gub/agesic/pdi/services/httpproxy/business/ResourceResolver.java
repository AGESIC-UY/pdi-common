package uy.gub.agesic.pdi.services.httpproxy.business;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResourceResolver {

    private static final Logger logger = LoggerFactory.getLogger(ResourceResolver.class);

    private ConfigService configService;

    private static HashMap<String, KeystoreInfo> keystoreMap = new HashMap<>();

    @Autowired
    public ResourceResolver(ConfigService configService) {
        this.configService = configService;
    }

    public KeystoreInfo getKeystore(String name, String password) throws Exception {
        KeystoreInfo ksInfo;
        if (keystoreMap.get(name) == null) {
            Response response = configService.configFile(name);
            KeyStore keyStore;
            InputStream is = null;
            try {
                is = response.body().asInputStream();
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(is, password.toCharArray());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw e;
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            ksInfo = new KeystoreInfo(password, keyStore);
            keystoreMap.put(name, ksInfo);
        } else {
            ksInfo = keystoreMap.get(name);
        }

        return ksInfo;
    }

    public Map<String, PropertyInfo> getProperties(String name) throws Exception {
        Map<String, PropertyInfo> propsError = new HashMap<>();
        Response response = configService.configFile(name);
        if (response != null && response.body() != null) {
            try (InputStream is = response.body().asInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                List<PropertyInfo> props = mapper.readValue(is, new TypeReference<List<PropertyInfo>>() { });
                if (props != null) {
                    for (PropertyInfo prop : props) {
                        propsError.put(prop.getCode(), prop);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        } else {
            logger.error("No se pudo acceder al archivo de errores errors.json");
        }

        return propsError;
    }

    public String getFileAsString(String name) throws Exception {
        Response response = configService.configFile(name);
        InputStream is = null;
        try {
            is = response.body().asInputStream();
            StringBuilder textBuilder = new StringBuilder();
            Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(StandardCharsets.UTF_8.name())));
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
            return textBuilder.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    class KeystoreInfo {
        private String pass;
        private KeyStore keystore;

        public KeystoreInfo(String pass, KeyStore keystore) {
            this.keystore = keystore;
            this.pass = pass;
        }

        public String getPass() {
            return pass;
        }

        public KeyStore getKeystore() {
            return keystore;
        }
    }

}
