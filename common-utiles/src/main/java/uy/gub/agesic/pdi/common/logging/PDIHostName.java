package uy.gub.agesic.pdi.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class PDIHostName {

    private static final Logger logger = LoggerFactory.getLogger(PDIHostName.class);

    public static String HOST_NAME;

    static {
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private PDIHostName() {
    }
}
