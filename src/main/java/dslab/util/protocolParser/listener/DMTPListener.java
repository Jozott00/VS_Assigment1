package dslab.util.protocolParser.listener;

import dslab.exception.ValidationException;
import dslab.model.Email;

import java.util.List;

public interface DMTPListener extends IProtocolListener {

    String begin();

    String subject(List<String> data);

    String data(List<String> data);

    String from(String from) throws ValidationException;

    String to(String to) throws ValidationException;

    String send() throws ValidationException;
}
