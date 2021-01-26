package model.hotelrequest;

import model.RequestData;

public class UpdateRoomStatusRequestData implements RequestData {
    private int roomNumber;
    private boolean occupied;
    private String guest;

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public String getGuest() { return guest; }
    public void setGuest(String guest) { this.guest = guest; }
}
