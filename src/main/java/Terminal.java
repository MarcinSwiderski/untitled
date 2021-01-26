import model.hotelrequest.*;
import model.hotelrequest.BookRoomResponseData.BookedRoom;
import model.hotelrequest.EndStayRequestData.RoomWithKey;
import model.roomrequest.*;
import model.roomrequest.RoomRequest.RequestType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Terminal {
    private JPanel cardPanel;
    private JTextField name;
    private JSpinner singleRoomsAmount;
//    private JSpinner doubleRoomsAmount;
//    private JSpinner tripleRoomsAmount;
    private JButton bookButton;
    private JPanel booked;
    private JButton closeReservation;

    private SocketClientUtil hotelSCU = new SocketClientUtil("127.0.0.1", Hotel.getHotelPort());

    private List<BookedRoom> bookedRooms;
    private Set<BookedRoom> occupiedRooms = new HashSet<>();

    public Terminal() {
        booked.setLayout(new BoxLayout(booked, BoxLayout.Y_AXIS));
        bookButton.addActionListener(actionEvent -> bookButton());
        closeReservation.addActionListener(actionEvent -> endBooking());
    }

    private void bookButton() {
        final String customerName = name.getText();
        try {
            for (JSpinner spinner : Arrays.asList(singleRoomsAmount))
                spinner.commitEdit();
        } catch (ParseException ignored) {}

        int singleRooms = (Integer) singleRoomsAmount.getValue();

        new Thread(() -> {
            List<Integer> roomSizes = new ArrayList<>();
            IntStream.range(0, singleRooms).mapToObj(i -> 1).forEach(roomSizes::add);

            BookRoomRequestData bookRoomRequestData = new BookRoomRequestData();
            bookRoomRequestData.setCustomerName(customerName);
            bookRoomRequestData.setBookedRoomSizes(roomSizes);

            try {
                BookRoomResponseData response = hotelSCU.query(
                        HotelRequest.fromReqData(HotelRequest.RequestType.BOOK_ROOM, bookRoomRequestData),
                        BookRoomResponseData.class);

                if(!response.isBookingSuccessful()) {
                    JOptionPane.showMessageDialog(cardPanel, "Hotel nie posiada wystarczającej liczby pokoi");
                    return;
                }

                SwingUtilities.invokeLater(() -> {
                    bookingSuccessful(response.getBookedRooms());
                });

            } catch (IOException e) {
                JOptionPane.showMessageDialog(cardPanel, "Błąd przy pytaniu hotelu o rezerwację");
            }
        }).start();
    }

    private List<RoomWithKey> getRoomsWithKeys() {
        if(bookedRooms == null)
            return new ArrayList<>();

        return bookedRooms.stream()
                .map(bookedRoom -> {
                    RoomWithKey roomWithKey = new RoomWithKey();
                    roomWithKey.setRoomNumber(bookedRoom.getNumber());
                    roomWithKey.setRoomKey(bookedRoom.getKey());
                    return roomWithKey;
                })
                .collect(Collectors.toList());
    }

    private void endBooking() {
        String who = name.getText();
        List<RoomWithKey> roomsWithKeys = getRoomsWithKeys();

        new Thread(() -> {
            try {
                EndStayRequestData endStayRequestData = new EndStayRequestData();
                endStayRequestData.setWho(who);
                endStayRequestData.setRoomsWithKeys(roomsWithKeys);
                endStayRequestData.setForce(false);

                EndStayResponseData resp = hotelSCU.query(
                        HotelRequest.fromReqData(HotelRequest.RequestType.END_STAY, endStayRequestData),
                        EndStayResponseData.class);

                SwingUtilities.invokeLater(() -> {
                    if(resp.isOk()) {
                        booked.removeAll();
                        bookedRooms = null;
                        switchCard("initialCard");
                    } else {
                        String message = "Nie można zakończyć rezerwacji.";

                        List<Integer> stillOccupiedRooms = resp.getStillOccupiedRooms();
                        if (stillOccupiedRooms != null && stillOccupiedRooms.size() == 1) {
                            message += String.format(" Pokój o numerze %s jest nadal zajęty.",
                                    stillOccupiedRooms.get(0));
                        } else if (stillOccupiedRooms != null && !stillOccupiedRooms.isEmpty()) {
                            message += String.format(" Pokoje o numerach %s są nadal zajęte.",
                                    stillOccupiedRooms.stream()
                                            .map(String::valueOf)
                                            .collect(Collectors.joining(", ")));
                        }

                        JOptionPane.showMessageDialog(cardPanel, message);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void switchCard(String cardName) {
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, cardName);
    }

    private void bookingSuccessful(List<BookedRoom> bookedRooms) {
        this.bookedRooms = bookedRooms;

        switchCard("bookedCard");

        for(BookedRoom bookedRoom : bookedRooms)
            addRoomToUi(bookedRoom);
    }

    private void addRoomToUi(BookedRoom bookedRoom) {
        Panel roomPanel = new Panel(new BorderLayout(10, 10));
        Button enterButton = new Button("Wejdź");
        enterButton.addActionListener(actionEvent -> {
            if(occupiedRooms.contains(bookedRoom)) {
                leaveRoomAsync(enterButton, bookedRoom);
            } else {
                enterRoomAsync(enterButton, bookedRoom);
            }
        });
        String firstLabel = String.format("Numer pokoju: %d (pokój %d-osobowy)",
                bookedRoom.getNumber(),
                bookedRoom.getSize());
        roomPanel.add(new Label(firstLabel), BorderLayout.LINE_START);
        roomPanel.add(new Label("Port pokoju: " + bookedRoom.getPort()), BorderLayout.CENTER);
        roomPanel.add(enterButton, BorderLayout.LINE_END);
        booked.add(roomPanel);
    }

    private void enterRoomAsync(Button enterButton, BookedRoom bookedRoom) {
        String who = name.getText();
        if(who == null || who.isEmpty()) {
            JOptionPane.showMessageDialog(cardPanel, "Nie wprowadzono nazwy rezerwującego.");
            return;
        }

        new Thread(() -> {
            EnterRequestData enterRequestData = new EnterRequestData();
            enterRequestData.setWho(who);
            enterRequestData.setKey(bookedRoom.getKey());

            try(SocketClientUtil scu = new SocketClientUtil(bookedRoom.getHost(), bookedRoom.getPort())) {
                EnterResponseData res = scu.query(
                        RoomRequest.fromReqData(RequestType.ENTER, enterRequestData),
                        EnterResponseData.class);

                if(res.isCorrectKey()) {
                    SwingUtilities.invokeLater(() -> {
                        occupiedRooms.add(bookedRoom);
                        enterButton.setLabel("Wyjdź");
                    });
                } else {
                    JOptionPane.showMessageDialog(cardPanel, "Zły klucz do pokoju");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // leaves a room asynchronously (doesn't make us wait for the Room and doesn't make Room wait for the Hotel)
    private void leaveRoomAsync(Button enterButton, BookedRoom bookedRoom) {
        String who = name.getText();

        new Thread(() -> {
            ExitRequestData exitRequestData = new ExitRequestData();
            exitRequestData.setWho(who);
            exitRequestData.setKey(bookedRoom.getKey());
            exitRequestData.setSynchronous(false); // don't need to wait for Hotel to know

            try(SocketClientUtil scu = new SocketClientUtil(bookedRoom.getHost(), bookedRoom.getPort())) {
                ExitResponseData res = scu.query(
                        RoomRequest.fromReqData(RequestType.EXIT, exitRequestData),
                        ExitResponseData.class);

                if(res.isCorrectKey()) {
                    SwingUtilities.invokeLater(() -> {
                        occupiedRooms.remove(bookedRoom);
                        enterButton.setLabel("Wejdź");
                    });
                } else {
                    JOptionPane.showMessageDialog(cardPanel, "Zły klucz do pokoju");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // leaves a room synchronously (wait for the Room and make Room wait for the Hotel to respond)
    private boolean leaveRoomSync(String who, BookedRoom bookedRoom) {
        ExitRequestData exitRequestData = new ExitRequestData();
        exitRequestData.setWho(who);
        exitRequestData.setSynchronous(true);
        exitRequestData.setKey(bookedRoom.getKey());
        try(SocketClientUtil scu = new SocketClientUtil(bookedRoom.getHost(), bookedRoom.getPort())) {
            return scu
                    .query(RoomRequest.fromReqData(RequestType.EXIT, exitRequestData), ExitResponseData.class)
                    .isCorrectKey();
        } catch (IOException e) {
            System.err.println("Failed leaving synchronously, the Room might already be dead:");
            e.printStackTrace();
            return false;
        }
    }

    private void endStaySync(String who, boolean shouldForce) throws IOException {
        EndStayRequestData endStayRequestData = new EndStayRequestData();
        endStayRequestData.setWho(who);
        endStayRequestData.setRoomsWithKeys(getRoomsWithKeys());
        endStayRequestData.setForce(shouldForce);
        hotelSCU.query(
                HotelRequest.fromReqData(HotelRequest.RequestType.END_STAY, endStayRequestData),
                EndStayResponseData.class);
    }

    private void forciblyEndStayOnExit() throws IOException, InterruptedException {
        String who = name.getText();

        final AtomicBoolean hasDeadRoom = new AtomicBoolean(false);
        List<Thread> leaveThreads = occupiedRooms.stream()
                .map(occupiedRoom -> new Thread(() -> {
                    if(!leaveRoomSync(who, occupiedRoom))
                        hasDeadRoom.set(true);
                }))
                .collect(Collectors.toList());

        for(Thread leaveThread : leaveThreads)
            leaveThread.start();

        for(Thread leaveThread : leaveThreads)
            leaveThread.join();

        // If some of out occupied rooms have already been dead before we've send an EXIT request to them,
        // the Hotel will not be able to end our stay correctly, leaving the system with a state that can't be
        // recovered from. To mitigate this, if any of our booked rooms are dead but still occupied, we tell the Hotel
        // that the booking should end forcefully, no matter if booked rooms exist or not.
        boolean shouldForceEndStay = hasDeadRoom.get();

        endStaySync(who, shouldForceEndStay);
    }

    private void onExit() {
        try {
            if(bookedRooms != null)
                forciblyEndStayOnExit();

            hotelSCU.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Terminal");
        frame.setContentPane(cardPanel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setMinimumSize(new Dimension(500, 250));
        frame.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                onExit();
                frame.dispose();
            }
        });
    }

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.runUi();
    }
}
