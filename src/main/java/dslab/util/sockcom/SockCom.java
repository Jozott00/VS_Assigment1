package dslab.util.sockcom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Encapsulates the communication through a given socket.
 */
public class SockCom implements ISockComm {

    Socket socket;
    BufferedReader in;
    PrintStream out;

    public SockCom(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintStream(socket.getOutputStream(), true);
    }

    @Override
    public String readLine() throws IOException {
        return in.readLine();
    }

    @Override
    public void writeLine(Object data) {
        out.println(data);
    }

    public PrintStream out() {
        return out;
    }
}
