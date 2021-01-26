package model.hotelrequest;

import model.Request;

public class RoomUnregisterReq implements Request {
    private int roomNumber;

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
}
