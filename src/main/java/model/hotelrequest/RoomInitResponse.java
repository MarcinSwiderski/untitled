package model.hotelrequest;

import model.ResponseData;

public class RoomInitResponse implements ResponseData {
    private int roomNumber;
    private int roomPort;
    private String roomKey;

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomPort() {
        return roomPort;
    }

    public void setRoomPort(int roomPort) {
        this.roomPort = roomPort;
    }
}
