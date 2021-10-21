package dslab.transfer;

import dslab.exception.DomainLookUpException;
import dslab.exception.NoOkResponseException;
import dslab.model.Email;
import dslab.model.ServerSpecificEmail;
import dslab.model.TransferSenderPreparation;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TransferSenderTask implements Runnable{

    ServerSpecificEmail email;
    Socket socket;
    SockCom comm;

    public TransferSenderTask(ServerSpecificEmail email) {
        this.email = email;
    }

    @Override
    public void run() {
        socket = connectToServer();
        try {
            comm = new SockCom(socket);
        } catch (IOException e) {
            throw new RuntimeException("Error initiating communication to server", e);
        }

        System.out.println("Run email to " + email.getRecipients() );

        comm.writeLine("begin");
        // TODO: On failure handling
        comm.readLine();
        int attemptCounter = 0;
        do {
            comm.writeLine("to " + String.join(",", email.getRecipients()));
        } while (!checkToResponse() && !email.isFailureMail() && attemptCounter++ < 3);

        comm.writeLine("data " + email.getData());
        comm.readLine();
        comm.writeLine("subject " + email.getSubject());
        comm.readLine();
        comm.writeLine("from " + email.getFrom());
        comm.readLine();
        comm.writeLine("send");
        try {
            checkOkResponse();
        } catch (NoOkResponseException e) {
            e.printStackTrace();
        }
        comm.writeLine("quit");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Socket connectToServer() {
        try {
            return new Socket(email.getMailboxIp(), email.getMailboxPort());
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to " + email.getMailboxIp() + ":" + email.getMailboxPort() , e);
        }
    }

    private void checkOkResponse() throws NoOkResponseException {
        String res = comm.readLine().trim();
        if(!res.startsWith("ok"))
            throw new NoOkResponseException();

    }

    private boolean checkToResponse() {
        String res = comm.readLine().trim();
        if(!res.startsWith("ok")) {

            if(email.isFailureMail())
                return false;

            String[] tmp = res.split(" ");
            if(List.of(tmp).contains("unknown") && List.of(tmp).contains("recipients")) {
                List<String> invalidNames = Arrays.stream(tmp[tmp.length - 1].split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

                List<String> invalidEmails = email.getRecipients()
                    .stream()
                    .filter(r -> invalidNames.contains(r.split("@")[0]))
                    .collect(Collectors.toList());

                email.getRecipients().removeAll(invalidEmails);

                String errorString = "error not existing emails " + String.join(",", invalidEmails);

                try {
                    ServerSpecificEmail failureMail = TransferSenderPreparation.createEmailDeliveryFailure(email.getFrom(), errorString);
                    TransferServer.getForwardPool().execute(new TransferSenderTask(failureMail));
                } catch (DomainLookUpException ignored) {}
            }

            return false;
        }

        return true;
    }




}
