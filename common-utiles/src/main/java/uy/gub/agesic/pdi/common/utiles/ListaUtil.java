package uy.gub.agesic.pdi.common.utiles;

import uy.gub.agesic.pdi.common.exceptions.PDIException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ListaUtil {

    public static <T> List<T> getSubList(List<T> items, int nroPagina, int tamanioPagina) throws PDIException {
        List<T> subLista = new ArrayList<T>();

        if (nroPagina < 0) {
            throw new PDIException("El numero de pagina solicitado no es valido: " + nroPagina);
        }
        if (tamanioPagina <= 0) {
            throw new PDIException("El tamanio de pagina solicitado no es valido: " + tamanioPagina);
        }

        if (items != null && !items.isEmpty()) {
            int fromIndex = tamanioPagina * nroPagina;
            int toIndex = tamanioPagina * (nroPagina + 1);

            if (toIndex > items.size()) {
                toIndex = items.size();
            }

            subLista = items.subList(fromIndex, toIndex);
        } else {
            subLista = items;
        }

        return subLista;
    }

    public static <T, V> List<T> filterList(String fieldName, V testValue, List<T> items, Acceptor<V> acceptor){
        if (items == null || items.isEmpty()) {
            return new ArrayList<T>();
        }

        List<T> resultado = items.stream()
                .filter(item -> {
                    V fieldValue = (V)PowerClass.getProperty(item, fieldName);
                    return acceptor.accept(fieldValue, testValue);
                })
                .collect(Collectors.toList());

        return resultado;
    }

}



/*

    private Boolean comparaStrings (String textoBuscado, String dondeBuscar){
        Boolean resultado = false;

        String[] palabras = textoBuscado.split("\\s+");
        for (String palabra : palabras) {
            if (dondeBuscar.contains(palabra)) {
                resultado = true;
            }
        }

        return resultado;
    }


 */