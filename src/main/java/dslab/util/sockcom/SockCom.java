package dslab.util.sockcom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Encapsulates the communication through a given socket.
 */
public class SockCom implements ISockComm {

    Socket socket;
    BufferedReader in;
    PrintWriter out;

    public SockCom(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public String readLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Could not read from stream", e);
        }
    }

    @Override
    public void writeLine(Object data) {
        out.println(data);
    }
}
