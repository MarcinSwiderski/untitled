package model.roomrequest;

import model.RequestData;

public class EnterRequestData extends RequestData {
    private String key;
    private String who;

    public String getWho() { return who; }
    public void setWho(String who) { this.who = who; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
