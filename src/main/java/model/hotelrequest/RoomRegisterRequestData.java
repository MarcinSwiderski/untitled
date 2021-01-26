package model.hotelrequest;

import model.RequestData;

public class RoomRegisterRequestData implements RequestData {
    private int size;

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}
