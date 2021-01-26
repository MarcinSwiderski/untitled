package model.hotelrequest;

import model.RequestData;

import java.util.List;

public class EndStayRequestData implements RequestData {
    public static class RoomWithKey {
        private int roomNumber;
        private String roomKey;

        public int getRoomNumber() { return roomNumber; }
        public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
        public String getRoomKey() { return roomKey; }
        public void setRoomKey(String roomKey) { this.roomKey = roomKey; }
    }

    private String who;

    private List<RoomWithKey> roomsWithKeys;

    // Used for situations when the Terminal window is closed before the user ended their stay properly
    // and some of the Terminal's occupied Rooms are already dead. In this situation the Terminal can't
    // contact all Room servers to unregister itself, so forcing the end of a stay is necessary
    private boolean force;

    public boolean isForce() { return force; }
    public void setForce(boolean force) { this.force = force; }
    public String getWho() { return who; }
    public void setWho(String who) { this.who = who; }
    public List<RoomWithKey> getRoomsWithKeys() { return roomsWithKeys; }
    public void setRoomsWithKeys(List<RoomWithKey> roomsWithKeys) { this.roomsWithKeys = roomsWithKeys; }
}
