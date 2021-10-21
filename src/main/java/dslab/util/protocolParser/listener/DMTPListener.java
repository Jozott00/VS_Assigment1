package dslab.util.protocolParser.listener;

import dslab.exception.ValidationException;
import dslab.model.Email;

import java.util.List;

public abstract class DMTPListener implements IProtocolListener {

    public void onBegin() {

    }

    public void onSubject(String subject) {

    }

    public void onData(String data) {

    }

    public void onFrom(String from) throws ValidationException {

    }

    public void onTo(List<String> to) throws ValidationException {

    }

    public abstract void onSend(Email email) throws ValidationException;
}
