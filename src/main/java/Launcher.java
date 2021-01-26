import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Launcher {
    private JPanel panel;
    private JSpinner hotelPort;
    private JSpinner singleRoomsCount;
    private JButton startHotelButton;
    private JButton startRoomsButton;
    private JButton startTerminalsButton;

    private final File javaExeutable;
    private final File jar;
    private final List<Process> processes = new ArrayList<>();

    public Launcher() {
        javaExeutable = getJavaExecutable();
        jar = getJar();

        hotelPort.setValue(1600);
        singleRoomsCount.setValue(2);
        startHotelButton.addActionListener(actionEvent -> startHotel());
        startRoomsButton.addActionListener(actionEvent -> startRooms());
        startTerminalsButton.addActionListener(actionEvent -> startTerminal());
    }

    private static File getJavaExecutable() {
        File javaHome = new File(System.getProperty("java.home"));
        File bin = new File(javaHome, "bin");

        if("\\".equals(File.separator))
            return new File(bin, "javaw.exe");
        else
            return new File(bin, "java");
    }

    private static File getJar() {
        try {
            // credit to https://stackoverflow.com/a/320595/3105260
            return new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void killAllSubprocesses() {
        processes.forEach(Process::destroy);
    }

    private void runUi() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("Launcher");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                killAllSubprocesses();
                frame.dispose();
            }
        });
        frame.setMinimumSize(new Dimension(430, 480));
    }

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.runUi();
    }

    private void startTerminal() {
        start(buildProcess("Terminal"));
    }

    private void start(ProcessBuilder processBuilder) {
        try {
            processes.add(processBuilder.start());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel, "Nie można wystartować procesu");
            e.printStackTrace();
        }
    }

    private void startRooms() {
        int singleCount = (int) singleRoomsCount.getValue();
        IntStream.range(0, singleCount).forEach(i -> start(buildRoomOfSize(1)));
    }

    private void startHotel() {
        start(buildProcess("Hotel"));
    }

    private ProcessBuilder buildRoomOfSize(int size) {
        ProcessBuilder pb = buildProcess("Room");
        pb.environment().put("LAB06_ROOM_SIZE", String.valueOf(size));
        return pb;
    }

    private ProcessBuilder buildProcess(String entryPoint) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> launcherExtraArguments = runtimeMXBean.getInputArguments();
        String classPath =
                runtimeMXBean.getClassPath()
                        + System.getProperty("path.separator")
                        + jar.getPath();

        List<String> command = new ArrayList<>();
        command.add(javaExeutable.toString());
        command.addAll(launcherExtraArguments);
        command.add("-cp");
        command.add(classPath);
        command.add(entryPoint);

        ProcessBuilder pb = new ProcessBuilder()
                .command(command)
                .inheritIO();

        pb.environment().put("LAB06_HOTEL_PORT", hotelPort.getValue().toString());
        return pb;
    }
}