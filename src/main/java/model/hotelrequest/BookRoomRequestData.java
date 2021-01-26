package model.hotelrequest;

import model.RequestData;

import java.util.ArrayList;
import java.util.List;

public class BookRoomRequestData implements RequestData {
    private String customerName;
    private List<Integer> bookedRoomSizes;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public List<Integer> getBookedRoomSizes() { return bookedRoomSizes; }
    public void setBookedRoomSizes(List<Integer> bookedRoomSizes) { this.bookedRoomSizes = bookedRoomSizes; }
}
