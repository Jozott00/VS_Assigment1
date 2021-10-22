package dslab.util.protocolParser.listener;

import dslab.exception.ValidationException;
import dslab.model.Email;
import dslab.model.StoredEmail;

import java.util.List;

public interface DMAPListener extends IProtocolListener {

    String login(String username, String password) throws ValidationException;

    String list() throws ValidationException;

    String show(Integer emailId) throws ValidationException;

    String delete(Integer emailId) throws ValidationException;

    String logout() throws ValidationException;

}
