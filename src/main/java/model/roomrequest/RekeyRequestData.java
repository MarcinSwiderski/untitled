package model.roomrequest;

import model.RequestData;

public class RekeyRequestData implements RequestData {
    private String key;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
