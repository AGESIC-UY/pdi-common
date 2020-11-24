package uy.gub.agesic.pdi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uy.gub.agesic.pdi.common.logging.PDILoggingAspect;

/**
 * Configuracion general comun a cualquier modulo que referencie esta biblioteca
 */
@Configuration
public class CommonConfig {

    @Bean
    public PDILoggingAspect pdiLoggingAspect() {
        PDILoggingAspect aspect = new PDILoggingAspect();
        return aspect;
    }

}
