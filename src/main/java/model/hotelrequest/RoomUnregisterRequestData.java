package model.hotelrequest;

import model.RequestData;

public class RoomUnregisterRequestData extends RequestData {
    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    private int roomNumber;
}
