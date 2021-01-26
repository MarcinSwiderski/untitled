package model.hotelrequest;

import model.RequestData;

import java.util.List;

public class BookRoomRequestData implements RequestData {
    private String customerName;
    private int roomsAmount;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getRoomsAmount() {
        return roomsAmount;
    }
    public void setRoomsAmount(int roomsAmount) {
        this.roomsAmount = roomsAmount;
    }
}
