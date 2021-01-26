package model.hotelrequest;

import model.RequestData;

public class UpdateRoomStatusRequestData implements RequestData {
    private int roomNumber;
    private String guest;

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public String getGuest() { return guest; }
    public void setGuest(String guest) { this.guest = guest; }
}
