import javax.swing.*;

public class Test extends JFrame {
    public static int PORT;

    public Test() {
        PORT = deductPort();
        Thread host = new Thread(() -> {
           new Hotel();
        });
        host.start();


    }

    public static void main(String[] args) {
        new Test();
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
