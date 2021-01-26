package model.hotelrequest;

import model.RequestData;

public class RoomUnregisterRequestData implements RequestData {
    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    private int roomNumber;
}
