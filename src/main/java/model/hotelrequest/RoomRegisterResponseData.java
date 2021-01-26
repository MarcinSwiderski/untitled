package model.hotelrequest;

import model.ResponseData;

public class RoomRegisterResponseData extends ResponseData {
    private int number;
    private int port;
    private String key;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
}
