package model.hotelrequest;

import model.RequestData;

public class RoomInitReqData implements RequestData {
    private int size;

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
}
