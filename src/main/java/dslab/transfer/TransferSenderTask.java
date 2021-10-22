package dslab.transfer;

import dslab.exception.DomainLookUpException;
import dslab.exception.NoOkResponseException;
import dslab.model.ServerSpecificEmail;
import dslab.model.TransferSenderPreparation;
import dslab.util.Config;
import dslab.util.sockcom.SockCom;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransferSenderTask implements Runnable{

    static protected List<TransferSenderTask> activeTasks = Collections.synchronizedList(new ArrayList<>());

    private ServerSpecificEmail email;
    private Socket socket;
    private SockCom comm;

    public TransferSenderTask(ServerSpecificEmail email) {
        this.email = email;
    }

    @Override
    public void run() {
        activeTasks.add(this);
        try {
            socket = connectToServer();
            comm = new SockCom(socket);

//            System.out.println("Run email to " + email.getRecipients());

            comm.readLine();
            comm.writeLine("begin");
            comm.readLine();

            int attemptCounter = 0;
            do {
                comm.writeLine("to " + String.join(",", email.getRecipients()));
            } while (!checkToResponse() && !email.isFailureMail() && attemptCounter++ < 3 && email.getRecipients().size() > 0);

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
                    sendFailureMail("error sending mail to " + String.join(",", email.getFrom()));
                e.printStackTrace();
            }
            comm.writeLine("quit");
            closeConnection();
        } catch (SocketException ignored) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        activeTasks.remove(this);
    }

    Socket connectToServer() {
        try {
            return new Socket(email.getMailboxIp(), email.getMailboxPort());
        } catch (IOException e) {
            sendFailureMail("could not connect to recipient server");
            throw new RuntimeException("Could not connect to " + email.getMailboxIp() + ":" + email.getMailboxPort() , e);
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
            ServerSpecificEmail failureMail = TransferSenderPreparation.createEmailDeliveryFailure(email.getFrom(), error);
            TransferServer.getForwardPool().execute(new TransferSenderTask(failureMail));
        } catch (DomainLookUpException ignored) {}
    }

    private void sendUdpData() {
        try {
            DatagramSocket socket = new DatagramSocket();

            Config config = TransferServer.config;

            String data = TransferServer.serverSocket.getInetAddress().getHostAddress()
                + ":"
                + TransferServer.serverSocket.getLocalPort()
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
