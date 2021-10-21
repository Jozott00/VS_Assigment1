package dslab.util.protocolParser;

public class ProtocolParseException extends Exception {

        public ProtocolParseException(String reason) {
            super("error "  + reason);
        }

}
