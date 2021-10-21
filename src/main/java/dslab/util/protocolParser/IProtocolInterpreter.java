package dslab.util.protocolParser;

public interface IProtocolInterpreter {

    String interpretRequest(String req) throws ProtocolParseException;

}
