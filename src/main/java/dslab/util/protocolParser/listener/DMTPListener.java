package dslab.util.protocolParser.listener;

import dslab.exception.ValidationException;
import dslab.model.Email;

import java.util.List;

public interface DMTPListener extends IProtocolListener {

    default void onBegin() {

    }

    default void onSubject(String subject) {

    }

    default void onData(String data) {

    }

    default void onFrom(String from) throws ValidationException {

    }

    default void onTo(List<String> to) throws ValidationException {

    }

    void onSend(Email email) throws ValidationException;
}
