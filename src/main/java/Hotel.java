import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import model.ResponseData;
import model.hotelrequest.*;
import model.hotelrequest.BookRoomResponseData.BookedRoom;
import model.hotelrequest.EndStayRequestData.RoomWithKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Hotel {
    public static class Room {
        public final AtomicInteger number = new AtomicInteger();
        public final AtomicInteger port = new AtomicInteger();
        public final AtomicReference<String> bookedCustomer = new AtomicReference<String>();
        public final AtomicReference<String> key = new AtomicReference<String>();
        public final AtomicReference<String> guestInside = new AtomicReference<String>();
    }
    private List<Room> rooms = new CopyOnWriteArrayList<>(); // a concurrent list so HotelGUI can access it freely
    private HotelGUI gui;

    public Hotel() {
        startGui();
        serve(getHotelPort());

    }

    public static void main(String[] args) {
        Hotel hotel = new Hotel();
    }

    private void startGui() {
        gui = new HotelGUI(rooms, getHotelPort());
        Thread guiThread = new Thread(gui);
        guiThread.start();
    }

    private void serve(final int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
            while (true) {
                // cannot use try-with-resources here as clientSocket would close before
                // handleConnection starts. clientSocket has to be closed in handleConnection
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed creating a server socket", e);
        }
    }

    public static int getHotelPort() {
        String port = System.getenv("HOTEL:PORT");
        if(port == null)
            return 1600;
        return Integer.parseInt(port);
    }

    private void handleConnection(Socket clientSocket) {
        try(PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String line;
            while(null != (line = in.readLine()) && !line.isEmpty())
                handleLine(line, out);

            clientSocket.close();
        } catch (IOException e) {
            // don't rethrow on errors local to only one connection
            e.printStackTrace();
        }
    }

    private synchronized void handleLine(String line, PrintWriter out) {
        HotelRequest request = JsonIterator.deserialize(line, HotelRequest.class);
        Any args = request.getArguments();
        ResponseData resp;
        switch (request.getType()) {
            case ROOM_REGISTER: resp = handleRoomRegister(args.as(RoomInitReqData.class)); break;
            case ROOM_UNREGISTER: resp = handleRoomUnregister(args.as(RoomUnregisterReq.class)); break;
            case BOOK_ROOM: resp = handleBookRoom(args.as(BookRoomRequestData.class)); break;
            case END_STAY: resp = handleEndStay(args.as(EndStayRequestData.class)); break;
            default:
                throw new RuntimeException("Unknown request type");
        }
        out.println(JsonStream.serialize(resp));
    }

    private EndStayResponseData handleEndStay(EndStayRequestData req) {
        EndStayResponseData endStayResponseData = new EndStayResponseData();
        String guest = req.getWho();

        List<Room> bookedForCustomer = rooms.stream()
                .filter(room -> guest.equals(room.bookedCustomer.get()))
                .collect(Collectors.toList());

        if(!req.isForce()) {
            List<Integer> bookedButStillOccupiedRoomNumbers = bookedForCustomer.stream()
                    .filter(room -> room.guestInside.get() != null)
                    .map(room -> room.number.get())
                    .collect(Collectors.toList());

            if (!bookedButStillOccupiedRoomNumbers.isEmpty()) {
                endStayResponseData.setOk(false);
                endStayResponseData.setStillOccupiedRooms(bookedButStillOccupiedRoomNumbers);
                return endStayResponseData;
            }

            for(RoomWithKey roomWithKey : req.getRoomsWithKeys()) {
                Optional<Room> room = rooms.stream()
                        .filter(r -> r.number.get() == roomWithKey.getRoomNumber())
                        .findFirst();

                if(room.isEmpty() || !room.get().key.get().equals(roomWithKey.getRoomKey())) {
                    endStayResponseData.setOk(false);
                    return endStayResponseData;
                }
            }
        }

        bookedForCustomer.forEach(room -> room.bookedCustomer.set(null));

        gui.notifyModified(rooms);
        endStayResponseData.setOk(true);
        return endStayResponseData;
    }

    private BookRoomResponseData handleBookRoom(BookRoomRequestData req) {
        String customerName = req.getCustomerName();
        int requestedRooms = req.getRoomsAmount();

        List<Room> availableRooms = rooms.stream()
                .filter(room -> room.bookedCustomer.get() == null)
                .collect(Collectors.toList());

            if(availableRooms.size() < requestedRooms) {
                BookRoomResponseData resp = new BookRoomResponseData();
                resp.setBookingSuccessful(false);
                return resp;
            }

        BookRoomResponseData resp = new BookRoomResponseData();
        resp.setBookingSuccessful(true);
        List<BookedRoom> roomsToBook = new ArrayList<>();
        for (int i = 0; i < requestedRooms; i++) {
            Room freeRoom = rooms.stream()
                    .filter(room -> room.bookedCustomer.get() == null)
                    .findFirst()
                    .orElseThrow();
            freeRoom.bookedCustomer.set(customerName);
            BookedRoom bookedRoom = new BookedRoom();
            bookedRoom.setHost("127.0.0.1");
            bookedRoom.setPort(freeRoom.port.get());
            bookedRoom.setNumber(freeRoom.number.get());
            bookedRoom.setKey(freeRoom.key.get());
            roomsToBook.add(bookedRoom);
        }
        resp.setBookedRooms(roomsToBook);

        gui.notifyModified(rooms);
        return resp;
    }

    private RoomUnregisterResponseData handleRoomUnregister(RoomUnregisterReq req) {
        rooms = rooms.stream()
                .filter(room -> room.number.get() != req.getRoomNumber())
                .collect(Collectors.toList());

        gui.notifyModified(rooms);
        return new RoomUnregisterResponseData();
    }

    private RoomInitResponse handleRoomRegister(RoomInitReqData request) {
        Room room = new Room();
        room.number.set(nextEmptyRoomNumber());
        room.port.set(nextEmptyRoomPort());
        room.key.set(UUID.randomUUID().toString());
        rooms.add(room);

        RoomInitResponse rrr = new RoomInitResponse();
        rrr.setRoomNumber(room.number.get());
        rrr.setRoomPort(room.port.get());
        rrr.setRoomKey(room.key.get());

        gui.notifyModified(rooms);
        return rrr;
    }

    private int nextEmptyRoomNumber() {
        if(rooms.isEmpty())
            return 1;
        return rooms.get(rooms.size() - 1).number.get() + 1;
    }

    private int nextEmptyRoomPort() {
        if(rooms.isEmpty())
            return getHotelPort() + 1;
        return rooms.get(rooms.size() - 1).port.get() + 1;
    }
}
