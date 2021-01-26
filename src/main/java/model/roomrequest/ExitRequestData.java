package model.roomrequest;

import model.RequestData;

public class ExitRequestData extends RequestData {
    private String key;
    private String who;
    // used when handling the closing of a Terminal window - in that case we want to wait for
    // a response from the Hotel before returning from the request
    private boolean synchronous;

    public String getWho() { return who; }
    public void setWho(String who) { this.who = who; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public boolean isSynchronous() { return synchronous; }
    public void setSynchronous(boolean synchronous) { this.synchronous = synchronous; }
}
