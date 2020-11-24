package uy.gub.agesic.pdi.common.git;

import uy.gub.agesic.pdi.common.exceptions.PDIException;

public interface GitManager {

    void enviarCambios(String message, String fileName) throws PDIException;
    void clonarRepositorio() throws PDIException;
    void recibirCambios() throws PDIException;
    String getAbsoluteLocalPath(String fileName) throws PDIException;
}

