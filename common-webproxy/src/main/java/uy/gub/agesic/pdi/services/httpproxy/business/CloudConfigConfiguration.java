package uy.gub.agesic.pdi.services.httpproxy.business;

import feign.Feign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
@Lazy
public class CloudConfigConfiguration {

    private String configServiceUrl;

    private DiscoveryClient discoveryClient;

    @Autowired
    public CloudConfigConfiguration(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Bean
    @RefreshScope
    public ConfigService configService() {
        if (configServiceUrl == null) {
            List<String> services = this.discoveryClient.getServices();
            for (String service : services) {
                if (service.equalsIgnoreCase("config-service")) {
                    this.discoveryClient.getInstances(service).stream()
                            .forEach(s -> {
                                configServiceUrl = s.getUri().toString();
                            });
                }
            }
        }

        ConfigService service = Feign.builder().target(ConfigService.class, this.configServiceUrl);

        return service;
    }

}
