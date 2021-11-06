package dslab.transfer;

import dslab.exception.DomainLookUpException;
import dslab.exception.NoOkResponseException;
import dslab.model.ServerSpecificEmail;
import dslab.model.TransferSenderPreparation;
import dslab.util.Config;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TransferSenderTask implements Runnable{

    private TransferRepository repo;

    private ServerSpecificEmail email;
    private Socket socket;
    private SockCom comm;

    public TransferSenderTask(ServerSpecificEmail email, TransferRepository repo) {
        this.email = email;
        this.repo = repo;
    }

    @Override
    public void run() {
        repo.getActiveSenderTasks().add(this);
        try {
            socket = connectToServer();
            comm = new SockCom(socket);

//            System.out.println("Run email to " + email.getRecipients());

            comm.readLine();
            comm.writeLine("begin");
            comm.readLine();

            sendEmail();

            comm.writeLine("quit");
            closeConnection();
        } catch (SocketException ignored) {
            System.out.println("Stop sending because of Socket is closed");
        } catch (IOException e) {
            e.printStackTrace();
        }

        repo.getActiveSenderTasks().remove(this);
    }

    private void sendEmail() throws IOException {
        int attemptCounter = 0;
        do {
            comm.writeLine("to " + String.join(",", email.getRecipients()));
        } while (!checkToResponse() && !email.isFailureMail() && attemptCounter++ < 2 && email.getRecipients().size() > 0);

        if(email.getRecipients().size() == 0) return;

        comm.writeLine("data " + email.getData());
        comm.readLine();
        comm.writeLine("subject " + email.getSubject());
        comm.readLine();
        comm.writeLine("from " + email.getFrom());
        comm.readLine();
        comm.writeLine("send");
        try {
            checkOkResponse();
            sendUdpData(); // on successfull sending
        } catch (NoOkResponseException e) {
            if(!email.isFailureMail())
                sendFailureMail("error sending mail " + String.join(",", email.getRecipients()));
        }
    }

    Socket connectToServer() {
        try {
            return new Socket(email.getMailboxIp(), email.getMailboxPort());
        } catch (IOException e) {
            sendFailureMail("could not connect to recipient server");
            throw new RuntimeException("Could not connect to " + email.getMailboxIp() + ":" + email.getMailboxPort() + ": " + e.getMessage() , e);
        }
    }

    public void closeConnection() {
        if(!socket.isClosed()) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkOkResponse() throws NoOkResponseException, IOException {
        String res = comm.readLine().trim();
        if(!res.startsWith("ok"))
            throw new NoOkResponseException();

    }

    private boolean checkToResponse() throws IOException {
        String res = comm.readLine().trim();
        if(!res.startsWith("ok")) {

            if(email.isFailureMail())
                return false;

            String[] tmp = res.split(" ");
            if(List.of(tmp).contains("unknown") && List.of(tmp).contains("recipient")) {
                List<String> invalidNames = Arrays.stream(tmp[tmp.length - 1].split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

                List<String> invalidEmails = email.getRecipients()
                    .stream()
                    .filter(r -> invalidNames.contains(r.split("@")[0]))
                    .collect(Collectors.toList());

                email.getRecipients().removeAll(invalidEmails);

                String errorString = "error not existing emails " + String.join(",", invalidEmails);

                sendFailureMail(errorString);
            }

            return false;
        }

        return true;
    }

    private void sendFailureMail(String error) {
        try {
            ServerSpecificEmail failureMail = TransferSenderPreparation.createEmailDeliveryFailure(email.getFrom(), error, repo.getServerSocket().getInetAddress().getHostAddress());
            repo.getForwardPool().execute(new TransferSenderTask(failureMail, repo));
        } catch (DomainLookUpException ignored) {}
    }

    private void sendUdpData() {
        try {
            DatagramSocket socket = new DatagramSocket();

            Config config = repo.getConfig();

            String data = repo.getServerSocket().getInetAddress().getHostAddress()
                + ":"
                + repo.getServerSocket().getLocalPort()
                + " "
                + this.email.getFrom();

            byte[] buffer = data.getBytes();

            InetSocketAddress address = new InetSocketAddress(
                config.getString("monitoring.host"),
                config.getInt("monitoring.port")
            );

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address);

            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
