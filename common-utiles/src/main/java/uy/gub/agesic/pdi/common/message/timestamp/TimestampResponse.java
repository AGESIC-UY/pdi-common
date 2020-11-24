package uy.gub.agesic.pdi.common.message.timestamp;


import java.io.Serializable;
import java.util.Calendar;

public class TimestampResponse implements Serializable {

    private Calendar timestamp;

    public Calendar getTimestamp() {
        return timestamp;
    }

    public TimestampResponse setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
        return this;
    }

}
