package model.hotelrequest;

import model.RequestData;

public class RoomUnregisterReq implements RequestData {
    private int roomNumber;

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }
}
