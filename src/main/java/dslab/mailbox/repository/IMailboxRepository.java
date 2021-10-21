package dslab.mailbox.repository;

import dslab.model.Email;

import java.util.HashMap;
import java.util.List;

public interface IMailboxRepository {


    void addEmailToUsers(Email email);

    void deleteEmailById(Long id);

    List<String> getAllUsers();
}
