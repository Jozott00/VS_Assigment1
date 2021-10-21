package dslab.util.sockcom;

import java.io.IOException;

public interface ISockComm {

    String readLine() throws IOException;

    void writeLine(Object data);
}
