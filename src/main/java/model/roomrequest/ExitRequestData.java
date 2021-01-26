package model.roomrequest;

import model.RequestData;

public class ExitRequestData implements RequestData {
    private String key;
    private String who;
    private boolean synchronous;

    public String getWho() { return who; }
    public void setWho(String who) { this.who = who; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public boolean isSynchronous() { return synchronous; }
    public void setSynchronous(boolean synchronous) { this.synchronous = synchronous; }
}
