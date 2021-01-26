import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;

import java.io.*;
import java.net.Socket;

public class SocketClientUtil implements Closeable {
    private final String adress;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public SocketClientUtil(String netAdress, int port) {
        this.adress = netAdress;
        this.port = port;
    }

    public synchronized <Req, Res> Res query(Req request, Class<Res> resClass) throws IOException {
        if(socket == null) {
            socket = new Socket(adress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        String serialized = JsonStream.serialize(request);
        System.out.format("sent to %s:%d -> %s\n", adress, port, serialized);
        out.println(serialized);
        String line = in.readLine();
        System.out.format("received response <- %s\n", line);
        Res response = JsonIterator.deserialize(line, resClass);
        return response;
    }

    @Override
    public synchronized void close() throws IOException {
        if(socket != null && !socket.isClosed()) {
            if(socket.isConnected()) {
                out.println("");
            }
            out.close();
            in.close();
            socket.close();
        }
    }
}
