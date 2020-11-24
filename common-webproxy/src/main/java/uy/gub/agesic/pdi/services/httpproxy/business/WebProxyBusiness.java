package uy.gub.agesic.pdi.services.httpproxy.business;

import uy.gub.agesic.pdi.common.message.canonical.Canonical;
import uy.gub.agesic.pdi.common.message.soap.SoapPayload;
import uy.gub.agesic.pdi.services.httpproxy.exceptions.WebProxyException;

public interface WebProxyBusiness {

    Canonical<SoapPayload> invokeEndpoint(Canonical<SoapPayload> message) throws WebProxyException;

}
