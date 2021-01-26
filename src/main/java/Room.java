
import model.hotelrequest.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static model.hotelrequest.HotelRequest.RequestType.*;

public class Room {
    private JPanel panel;
    private JLabel labelName;
//    private JLabel labelCapacity;
    private JLabel labelIsFull;
    private JLabel labelKey;
    private Thread serverThread;
    private JFrame frame;
    private final int SIZE = 1;

    private SocketClientUtil hotelSCU = new SocketClientUtil("127.0.0.1", Hotel.getHotelPort());
    private final AtomicReference<ServerSocket> serverSocket = new AtomicReference<>();
    private final AtomicInteger number = new AtomicInteger();
    private final AtomicInteger port = new AtomicInteger();
    private final AtomicReference<String> key = new AtomicReference<>();
    private final AtomicReference<String> customerInside = new AtomicReference<>();

    private void updateLabels() {
        SwingUtilities.invokeLater(() -> { // run on GUI thread
            labelName.setText(String.valueOf(number.get()));
//            labelCapacity.setText(String.valueOf(SIZE));
            labelIsFull.setText(customerInside.get() == null ? "Wolny" : "Zajęty (" + customerInside.get() + ")");
            labelKey.setText(key.get());
        });
    }

    private void onExit() {
        // runs on the GUI thread
        Thread unregisterThread = new Thread(this::deleteFromHotel);
        unregisterThread.start();

        frame.dispose();

        try {
            hotelSCU.close();

            // ensure that we don't close a null ServerSocket
            serverSocket.compareAndExchange(null, new ServerSocket()).close();
            serverThread.join();
            unregisterThread.join();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void runUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame("Room");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                onExit();
            }
        });
        frame.setMinimumSize(new Dimension(500, 200));
    }

    public static void main(String[] args) {
        Room room = new Room();
        room.runUi();
        room.server();
    }

    private void queryNumberAndPort() throws IOException {
        RoomInitReqData rrr = new RoomInitReqData();
        rrr.setSize(SIZE);

        RoomInitResponse resp = hotelSCU.query(
                HotelRequest.fromReqData(ROOM_REGISTER, rrr),
                RoomInitResponse.class);

        setNumber(resp.getRoomNumber());
        setPort(resp.getRoomPort());
        setKey(resp.getRoomKey());
    }

    private void deleteFromHotel() {
        RoomUnregisterReq rurd = new RoomUnregisterReq();
        rurd.setRoomNumber(number.get());

        try {
            hotelSCU.query(
                    HotelRequest.fromReqData(ROOM_UNREGISTER, rurd),
                    RoomInitResponse.class);
        } catch (IOException e) {
            System.err.println("Failed unregistering from the Hotel");
            e.printStackTrace();
        }
    }

    private void server() {
        serverThread = new Thread(this::serverThread);
        serverThread.start();
    }

    private void setNumber(int number) {
        this.number.set(number);
        updateLabels();
    }

    private void setPort(int port) {
        this.port.set(port);
        updateLabels();
    }

    private void setKey(String key) {
        this.key.set(key);
        updateLabels();
    }

    private void serverThread() {
        try {
            queryNumberAndPort();

            ServerSocket serverSock = new ServerSocket();
            serverSocket.set(serverSock);

            serverSock.setReuseAddress(true);
            serverSock.bind(new InetSocketAddress(port.get()));
        } catch (SocketException sex) {
            // Ignore - the socket must have been closed from another thread
        } catch (IOException e) {
            throw new RuntimeException("Failed creating a server socket on a Room", e);
        }
    }
}