package dslab.util.worker;

import at.ac.tuwien.dsg.orvell.Context;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.exception.ExecutionStopException;
import dslab.exception.ValidationException;
import dslab.model.Email;
import dslab.util.protocolParser.ProtocolParseException;
import dslab.util.protocolParser.ProtocolParser;
import dslab.util.protocolParser.listener.DMTPListener;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DMTPWorker extends TCPWorker implements DMTPListener {

    private boolean began = false;

    private final Email email = new Email();

    public DMTPWorker(Socket clientSocket) {
        super(clientSocket, "ok DMTP");
    }

    @Override
    @Command
    public String begin() {
        this.began = true;
        return "ok";
    }

    @Override
    @Command
    public String subject(List<String> subject) {
        validateBegin();
        if(subject.isEmpty()) throw new ValidationException("subject expected");
        email.setSubject(String.join(" ", subject));
        return "ok";
    }

    @Override
    @Command
    public String data(List<String> data) {
        validateBegin();
        if(data.isEmpty()) throw new ValidationException("data expected");
        email.setData(String.join(" ", data));
        return "ok";
    }

    @Override
    @Command
    public String from(String from) throws ValidationException {
        validateBegin();
        email.setFrom(from);
        return "ok";
    }

    @Override
    @Command
    public String to(String to) throws ValidationException {
        validateBegin();
        List<String> recipients = Arrays.stream(to.split(","))
            .map(String::trim)
            .collect(Collectors.toList());
        email.setRecipients(recipients);
        return "ok " + recipients.size();
    }

    public void validateBegin() {
        if(!began) throw new ProtocolParseException();
    }

    protected Email getEmail() {
        return email;
    }

}
