package model.hotelrequest;

import model.Response;

public class TerminalEndReservationRes implements Response {
    private boolean ok;
    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }
}
