package model.roomrequest;

import model.ResponseData;

public class ExitResponseData implements ResponseData {
    private boolean correctKey;

    public boolean isCorrectKey() { return correctKey; }
    public void setCorrectKey(boolean correctKey) { this.correctKey = correctKey; }
}
