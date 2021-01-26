package model.hotelrequest;

import model.ResponseData;

import java.util.ArrayList;
import java.util.List;

public class BookRoomResponseData implements ResponseData {
    public static class BookedRoom {
        private int number;
        private String host;
        private int port;
        private String key;
        private int size;

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public int getNumber() { return number; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public void setNumber(int number) { this.number = number; }
        public void setHost(String host) { this.host = host; }
        public void setPort(int port) { this.port = port; }
    }

    private boolean bookingSuccessful;

    private List<BookedRoom> bookedRooms = new ArrayList<>();

    public boolean isBookingSuccessful() { return bookingSuccessful; }
    public void setBookingSuccessful(boolean bookingSuccessful) { this.bookingSuccessful = bookingSuccessful; }
    public List<BookedRoom> getBookedRooms() { return bookedRooms; }
    public void setBookedRooms(List<BookedRoom> bookedRooms) { this.bookedRooms = bookedRooms; }
}
