import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HotelGUI implements Runnable, TableModel {
    private JFrame frame;
    private JPanel panel;
    private JTable table;
    private JLabel portLabel;
    private List<Hotel.Room> rooms;
    private List<TableModelListener> listeners = new ArrayList<>();

    private final int COLUMN_ROOM_NUMBER = 0;
    private final int COLUMN_PORT = 1;
    private final int COLUMN_BOOKED = 2;


    public HotelGUI(List<Hotel.Room> rooms, int hotelPort) {
        this.rooms = rooms;
        this.portLabel.setText("Port: "+ hotelPort);
    }

    @Override
    public void run() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        frame = new JFrame("Hotel-Host");
        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        table.setModel(this);
        frame.setMinimumSize(new Dimension(540, 540));
    }

    @Override
    public int getRowCount() {
        return rooms.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int i) {
        return i == COLUMN_ROOM_NUMBER ? "Numer pokoju"
                : i == COLUMN_PORT ? "Port"
                : i == COLUMN_BOOKED ? "Zarezerwowany"
                : "";
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public String getValueAt(int row, int column) {
        if(row >= rooms.size())
            return "";
        Hotel.Room room = rooms.get(row);
        String booked = room.bookedCustomer.get();

        return column == COLUMN_ROOM_NUMBER ? room.number.toString()
                : column == COLUMN_PORT ? room.port.toString()
                : column == COLUMN_BOOKED ? (booked != null ? "tak (" + booked + ")" : "nie")
                : "";
    }

    @Override
    public void setValueAt(Object o, int i, int i1) {}

    @Override
    public void addTableModelListener(TableModelListener tableModelListener) {
        listeners.add(tableModelListener);
    }

    @Override
    public void removeTableModelListener(TableModelListener tableModelListener) {
        listeners.remove(tableModelListener);
    }

    public void notifyModified(final List<Hotel.Room> replacementRooms) {
        SwingUtilities.invokeLater(() -> {
            if(replacementRooms != null)
                this.rooms = replacementRooms;
            TableModelEvent tableModelEvent = new TableModelEvent(this);
            listeners.forEach(tableModelListener ->
                    tableModelListener.tableChanged(tableModelEvent));
        });
    }
}
