package uy.gub.agesic.pdi.common.utiles;

public interface Acceptor<T> {
    public Boolean accept(T candidateValue, T testValue);
}
