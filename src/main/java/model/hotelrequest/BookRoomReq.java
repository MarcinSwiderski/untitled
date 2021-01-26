package model.hotelrequest;

import model.Request;

public class BookRoomReq implements Request {
    private String name;
    private int roomsAmount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRoomsAmount() {
        return roomsAmount;
    }
    public void setRoomsAmount(int roomsAmount) {
        this.roomsAmount = roomsAmount;
    }
}
