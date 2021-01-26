import model.hotelrequest.*;
import model.hotelrequest.BookRoomResponseData.BookedRoom;
import model.hotelrequest.EndStayRequestData.RoomWithKey;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


public class Terminal {
    private JPanel cardPanel;
    private JTextField name;
    private JSpinner roomsAmount;
    private JButton bookButton;
    private JButton closeReservation;

    private SocketClientUtil hotelSCU = new SocketClientUtil("127.0.0.1", Hotel.getHotelPort());

    private List<BookedRoom> bookedRooms;

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
    }

    public Terminal() {
        bookButton.addActionListener(actionEvent -> bookButton());
        closeReservation.addActionListener(actionEvent -> endBooking());
        runUi();
    }

    private void bookButton() {
        final int roomsAmount = (Integer) this.roomsAmount.getValue();

        if (roomsAmount <= 0)
            return;

        final String customerName = name.getText();

        new Thread(() -> {
            BookRoomRequestData bookRoomRequestData = new BookRoomRequestData();
            bookRoomRequestData.setCustomerName(customerName);
            bookRoomRequestData.setRoomsAmount(roomsAmount);

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
                        bookedRooms = null;
                        switchCard("initialCard");
                    } else {
                        String message = "Nie można zakończyć rezerwacji.";
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
    }

    private void onExit() {
        try {
            hotelSCU.close();
        } catch (IOException e) {
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
}
