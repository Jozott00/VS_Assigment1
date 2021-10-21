package dslab.util.protocolParser;

import dslab.exception.ValidationException;
import dslab.model.Email;
import dslab.util.protocolParser.listener.DMTPListener;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DMTPInterpreter implements IProtocolInterpreter{

    private boolean hasBegan = false;
    private final Email email = new Email();

    private final DMTPListener listener;

    public DMTPInterpreter(DMTPListener listener) {
        this.listener = listener;
    }

    // TODO if undefined command before begin -> close connection
    @Override
    public String interpretRequest(String req) throws ProtocolParseException {

        req = req.trim().toLowerCase();

        if(req.equals("quit")) {
            listener.onQuit();
            return ok();
        }

        if(req.equals("begin")) {
            //TODO: what if hasBegan already true?
            hasBegan = true;
            listener.onBegin();
            return ok();
        }

        if(!hasBegan) throw new ProtocolParseException("protocol error");

        if(req.equals("send")) {
            try {
                email.valid();
                listener.onSend(this.email);
                return ok();
            } catch (ValidationException e) {
                throw new ProtocolParseException(e.getMessage());
            }
        }


        Optional<String> arg;

        if((arg = matches("to ", req)).isPresent()) {

            argNotBlank(arg.get());
            List<String> recipients = Arrays.stream(arg.get().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

            try {
                listener.onTo(recipients);
                this.email.setRecipients(recipients);
            } catch (ValidationException e) {
                throwPPEx(e.getMessage());
            }
            return ok() + " " + email.getRecipients().size();
        }


        if((arg = matches("from ", req)).isPresent()) {
            argNotBlank(arg.get());

            try {
                listener.onFrom(arg.get());
                email.setFrom(arg.get());
            } catch (ValidationException e) {
                throwPPEx(e.getMessage());
            }

            return ok();
        }

        if((arg = matches("subject ", req)).isPresent()) {
            argNotBlank(arg.get());
            email.setSubject(arg.get());
            listener.onSubject(arg.get());
            return ok();
        }

        if((arg = matches("data ", req)).isPresent()) {
            argNotBlank(arg.get());
            email.setData(arg.get());
            listener.onData(arg.get());
            return ok();
        }


        // if command is undefined
        listener.onQuit();
        throw new ProtocolParseException("protocol error");
    }

    public Email getEmail() {
        return email;
    }

    private String ok() {
        return "ok";
    }

    private Optional<String> matches(String key, String req) {
        if(!req.startsWith(key)) return Optional.empty();
        return Optional.of(req.substring(key.length()).trim());
    }

    private void argNotBlank(String arg) throws ProtocolParseException {
        if(arg.isBlank()) throwPPEx("no argument");
    }

    private void emailValidation(String email) throws ProtocolParseException {
        if(!email.matches("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"))
            throwPPEx("includes invalid email");
    }

    private void throwPPEx(String reason) throws ProtocolParseException {
        throw new ProtocolParseException(reason);
    }

}
