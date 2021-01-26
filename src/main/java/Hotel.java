import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import model.Response;
import model.hotelrequest.*;
import model.hotelrequest.BookRoomRes.BookedRoom;
import model.hotelrequest.TerminalEndReservationReq.RoomWithKey;
import javax.swing.*;
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
        public final AtomicReference<String> bookedCustomer = new AtomicReference<>();
        public final AtomicReference<String> key = new AtomicReference<>();
        public final AtomicReference<String> guestInside = new AtomicReference<>();
    }

    private List<Room> rooms = new CopyOnWriteArrayList<>(); // a concurrent list so HotelGUI can access it freely
    private HotelGraphicInterface gui;
    private static int startingPort = 3010;

    public Hotel() {
        startGui();
        serve(getHotelPort());
    }

    public Hotel(int port) {
        startingPort = port;
        startGui();
        serve(getHotelPort());
    }

    public static void main(String[] args) {
        Hotel hotel = new Hotel(Integer.parseInt(JOptionPane.showInputDialog("Port serwera: ")));
    }

    private void startGui() {
        gui = new HotelGraphicInterface(rooms, getHotelPort());
        Thread guiThread = new Thread(gui);
        guiThread.start();
    }

    private void serve(final int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed creating a server socket", e);
        }
    }

    public static int getHotelPort() {
        String port = System.getenv("HOTEL:PORT");
        if (port == null)
            return startingPort;
        return Integer.parseInt(port);
    }

    private void handleConnection(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String line;
            while (null != (line = in.readLine()) && !line.isEmpty())
                handleLine(line, out);

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void handleLine(String line, PrintWriter out) {
        HotelReq request = JsonIterator.deserialize(line, HotelReq.class);
        Any args = request.getArguments();
        Response resp;
        switch (request.getType()) {
            case ROOM_CREATED:
                resp = handleRoomRegister(args.as(RoomInitReq.class));
                break;
            case ROOM_REMOVED:
                resp = handleRoomUnregister(args.as(RoomUnregisterReq.class));
                break;
            case TERMINAL_RESERVE_ROOM:
                resp = handleBookRoom(args.as(BookRoomRequest.class));
                break;
            case TERMINAL_END_RESERVATION:
                resp = handleEndStay(args.as(TerminalEndReservationReq.class));
                break;
            default:
                throw new RuntimeException("Unknown request type");
        }
        out.println(JsonStream.serialize(resp));
    }

    private TerminalEndReservationRes handleEndStay(TerminalEndReservationReq req) {
        TerminalEndReservationRes endStayResponseData = new TerminalEndReservationRes();
        String guest = req.getWho();

        List<Room> bookedForCustomer = rooms.stream()
                .filter(room -> guest.equals(room.bookedCustomer.get()))
                .collect(Collectors.toList());

        List<Integer> bookedButStillOccupiedRoomNumbers = bookedForCustomer.stream()
                .filter(room -> room.guestInside.get() != null)
                .map(room -> room.number.get())
                .collect(Collectors.toList());

        if (!bookedButStillOccupiedRoomNumbers.isEmpty()) {
            endStayResponseData.setOk(false);
            endStayResponseData.setStillOccupiedRooms(bookedButStillOccupiedRoomNumbers);
            return endStayResponseData;
        }

        for (RoomWithKey roomWithKey : req.getRoomsWithKeys()) {
            Optional<Room> room = rooms.stream()
                    .filter(r -> r.number.get() == roomWithKey.getRoomNumber())
                    .findFirst();

            if (room.isEmpty() || !room.get().key.get().equals(roomWithKey.getRoomKey())) {
                endStayResponseData.setOk(false);
                return endStayResponseData;
            }
        }

        bookedForCustomer.forEach(room -> room.bookedCustomer.set(null));

        gui.notifyModified(rooms);
        endStayResponseData.setOk(true);
        return endStayResponseData;
    }

    private BookRoomRes handleBookRoom(BookRoomRequest req) {
        String customerName = req.getName();
        int requestedRooms = req.getRoomsAmount();

        List<Room> availableRooms = rooms.stream()
                .filter(room -> room.bookedCustomer.get() == null)
                .collect(Collectors.toList());

        if (availableRooms.size() < requestedRooms) {
            BookRoomRes resp = new BookRoomRes();
            resp.setBookingSuccessful(false);
            return resp;
        }

        BookRoomRes resp = new BookRoomRes();
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

    private RoomUnregisterRes handleRoomUnregister(RoomUnregisterReq req) {
        rooms = rooms.stream()
                .filter(room -> room.number.get() != req.getRoomNumber())
                .collect(Collectors.toList());

        gui.notifyModified(rooms);
        return new RoomUnregisterRes();
    }

    private RoomInitRes handleRoomRegister(RoomInitReq request) {
        Room room = new Room();
        room.number.set(nextEmptyRoomNumber());
        room.port.set(nextEmptyRoomPort());
        room.key.set(UUID.randomUUID().toString());
        rooms.add(room);

        RoomInitRes rrr = new RoomInitRes();
        rrr.setRoomNumber(room.number.get());
        rrr.setRoomPort(room.port.get());
        rrr.setRoomKey(room.key.get());

        gui.notifyModified(rooms);
        return rrr;
    }

    private int nextEmptyRoomNumber() {
        if (rooms.isEmpty())
            return 1;
        return rooms.get(rooms.size() - 1).number.get() + 1;
    }

    private int nextEmptyRoomPort() {
        if (rooms.isEmpty())
            return getHotelPort() + 1;
        return rooms.get(rooms.size() - 1).port.get() + 1;
    }
}
