package uy.gub.agesic.pdi.services.httpproxy.business;

import feign.*;
import uy.gub.agesic.pdi.common.message.canonical.Canonical;

import java.net.URI;

public interface ConfigService {

    @RequestLine("GET /*/default/master/{resourceName}")
    @Headers("Accept: application/octet-stream")
    Response configFile(@Param("resourceName") String resourceName);

}
