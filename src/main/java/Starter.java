import javax.swing.*;
import java.awt.*;

public class Starter extends JFrame {
    private JPanel panel;
    private JButton startRoomsButton;
    private JButton startTerminalsButton;
    public static int PORT;

    public Starter() {
        PORT = deductPort();
        Thread host = new Thread(() -> {
           new Hotel();
        });
        startRoomsButton.addActionListener(actionEvent -> addRooms());
        startTerminalsButton.addActionListener(actionEvent -> addTerminal());

        host.start();

        setContentPane(panel);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setVisible(true);
        setMinimumSize(new Dimension(430, 480));


    }
    public void addRooms(){
        Thread room = new Thread(() -> {
            new Room();
        });
        room.start();
    }
    public void addTerminal(){
        Thread terminal = new Thread(() -> {
            new UserInterface();
        });
        terminal.start();
    }

    public static void main(String[] args) {
        new Starter();
    }

    private int deductPort() {
        String port;
        try {
            port = System.getenv("HOTEL:PORT");
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            port = JOptionPane.showInputDialog("Port serwera: ");
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return  1600;
    }
}
