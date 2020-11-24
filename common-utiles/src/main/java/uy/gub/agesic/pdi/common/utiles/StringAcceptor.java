package uy.gub.agesic.pdi.common.utiles;

public class StringAcceptor implements Acceptor<String> {

    @Override
    public Boolean accept(String candidateValue, String testValue) {
        if (candidateValue == null && testValue == null) {
            return true;
        } else if (candidateValue != null && testValue == null) {
            return true;
        } else if (candidateValue == null && testValue != null) {
            return false;
        } else if (candidateValue != null && testValue != null) {
            candidateValue = candidateValue.toLowerCase();
            testValue =  testValue.toLowerCase();
            return candidateValue.contains(testValue);
        }

        return false;
    }
}
