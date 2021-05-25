import java.net.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * CMultiPeer
 * Peer to Peer UDP Multicasting Template
 *
 * Shows how two or more peers can communicate with each other
 * see https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/net/MulticastSocket.html
 * (c)2018-21 by Sven Nueesch
 *
 */

/**
 * CMultiPeer
 */
public class CMultiPeer extends Thread {

    private static final String MULTICAST_IP = "224.0.0.1";         // see https://en.wikipedia.org/wiki/Multicast_address
    private static final int PORT = 4434;                           // any port which is not used will do?? 4434-4440 was 4446
    private static final int BUFSIZE = 256;

    private CopyOnWriteArrayList<String> ReceivedMsgs;              // Thread safe ArrayList
    private DatagramSocket DataSocket;
    private MulticastSocket MultiSocket;
    private InetSocketAddress GroupAddr;
    private NetworkInterface NetworkIf;
    private volatile boolean Running = false;
    private char Peer;

    /**
     * MultiPeer Constructor
     * Sets up a receiver and a sender socket
     * @param peer character to identify which attendee we are
     */
    public CMultiPeer(char peer) {
        Peer = peer;
        ReceivedMsgs = new CopyOnWriteArrayList<>();
        try {
            DataSocket = new DatagramSocket();                      // used to send data            
            MultiSocket = new MulticastSocket(PORT);                // used to receive data, defines the port to listen to on the local network card

            
            // we use same network interface as localhost uses or use getByIndex(1) returns lo0 (index 1), en0 (index 4) both work 
            // or use getByInetAddress(InetAddress.getLocalHost()) returns lo0
            // or more universal but more complicated use my own method getIntIntfIdx() to get the interface connected to the internet (see MyPeerToPeer)
            int intfIdx = 1;
            MultiSocket.setNetworkInterface(NetworkInterface.getByIndex(intfIdx));
            System.out.println("Using NetworkInterface: " + MultiSocket.getNetworkInterface().getName());
            
            GroupAddr = new InetSocketAddress(InetAddress.getByName(MULTICAST_IP),PORT);
            NetworkIf = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

            MultiSocket.joinGroup(GroupAddr, NetworkIf);            // join a multicast group
            
        } catch (IOException ex) {
            System.out.println("Something happened!");
            Logger.getLogger(CMultiPeer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    /**
     * Sends a message to the multicast group
     * @param msg the message string to be sent
     */
    public void sendMessage(String msg) {
        try {
            System.out.println(Peer+"S: Sending  '" + msg + "' to Address: " + GroupAddr.getAddress().getHostAddress() + ":" + PORT);
            byte[] sndBuf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(sndBuf, sndBuf.length, InetAddress.getByName(MULTICAST_IP), PORT);
            DataSocket.send(packet);

        } catch (IOException ex) {
            Logger.getLogger(CMultiPeer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Thread run method
     * Asynchronously receives messages from the multicast group and fills them into a received message list
     */
    @Override
    public void run() {
        Running = true;
        System.out.println(Peer+"R: Receiver started.");
        while (Running) {
            try {
                byte[] rcvBuf = new byte[BUFSIZE];
                DatagramPacket packet = new DatagramPacket(rcvBuf, rcvBuf.length);
                MultiSocket.receive(packet);
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String rcvMsg = new String(packet.getData(), 0, packet.getLength());
                System.out.println(Peer+"R: Received '" + rcvMsg + "' at Address: " + address.getHostAddress() + ":" + port);
                if (rcvMsg.equals("#END#")) {
                    Running = false;
                    ReceivedMsgs.clear();
                } else {
                    ReceivedMsgs.add(rcvMsg);
                }
            } catch (IOException ex) {
                Logger.getLogger(CMultiPeer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Fetches the next message from the received message list
     * @return Received message
     */
    public String getMessage() {
        String s = null;
        if (!ReceivedMsgs.isEmpty()) {
            s = ReceivedMsgs.get(0);
            ReceivedMsgs.remove(0);
        }
        return s;
    }
    
    /**
     * Shows if receiver thread is Running
     * @return true if thread is Running
     */
    public boolean isRunning() {
        return Running;
    }

    /**
     * Shuts down peer communication
     */
    public void close() {
        MultiSocket.close();
        DataSocket.close();
        System.out.println(Peer+": shut down");
    }
    
}
