package model.hotelrequest;

import model.Response;

import java.util.List;

public class TerminalEndReservationRes implements Response {
    private boolean ok;
    private List<Integer> stillOccupiedRooms;

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }
    public List<Integer> getStillOccupiedRooms() { return stillOccupiedRooms; }
    public void setStillOccupiedRooms(List<Integer> stillOccupiedRooms) { this.stillOccupiedRooms = stillOccupiedRooms; }
}
