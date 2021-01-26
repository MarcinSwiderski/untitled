package model.hotelrequest;

import model.Request;

import java.util.List;

public class TerminalEndReservationReq implements Request {
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


    public String getWho() { return who; }
    public void setWho(String who) { this.who = who; }
    public List<RoomWithKey> getRoomsWithKeys() { return roomsWithKeys; }
    public void setRoomsWithKeys(List<RoomWithKey> roomsWithKeys) { this.roomsWithKeys = roomsWithKeys; }
}
