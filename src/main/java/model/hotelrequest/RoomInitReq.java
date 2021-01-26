package model.hotelrequest;

import model.Request;

public class RoomInitReq implements Request {
    private int size;

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}
