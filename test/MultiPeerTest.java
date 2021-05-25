/*
 * MultiPeerTest
 * Peer to Peer Multicast Template Tester
 * Tests the project. Use the Test Project Button or type Ctrl-F6 to run.
 * (c)2018 by Sven Nueesch
 *
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MultiPeerTest
 * @author Sven Nueesch
 */
public class MultiPeerTest {

    CMultiPeer PeerA;
    CMultiPeer PeerB;

    public MultiPeerTest() {
    }

    @Before
    public void setUp() {
        PeerA = new CMultiPeer('A');
        PeerA.start();
        PeerB = new CMultiPeer('B');
        PeerB.start();
        while (!PeerA.isRunning() || !PeerB.isRunning()) {
            wait(100);
        }
    }

    @Test
    public void whenCanSendAndReceivePacket_thenCorrect() {
        PeerA.sendMessage("Hello B, how are you?");
        wait(100);
        String rcvd = PeerB.getMessage();
        assertEquals("Hello B, how are you?", rcvd);
        assertNotEquals(null, rcvd);
        rcvd = PeerA.getMessage();
        assertEquals("Hello B, how are you?", rcvd);
        assertNotEquals(null, rcvd);

        PeerB.sendMessage("Hello A, how are you?");
        wait(100);
        rcvd = PeerA.getMessage();
        assertEquals("Hello A, how are you?", rcvd);
        assertNotEquals(null, rcvd);

        PeerA.sendMessage("Work for you");
        wait(100);
        rcvd = PeerB.getMessage();
        assertEquals("Hello A, how are you?", rcvd);
        assertNotEquals(null, rcvd);
        rcvd = PeerB.getMessage();
        assertEquals("Work for you", rcvd);
        assertFalse(rcvd.equals("Hello A, how are you?"));
        assertNotEquals(null, rcvd);

        PeerA.sendMessage("#END#");
        wait(100);
        rcvd = PeerB.getMessage();
        assertEquals(null, rcvd);
        rcvd = PeerB.getMessage();
        assertEquals(null, rcvd);
        
        assertFalse(PeerA.isRunning());
        assertFalse(PeerB.isRunning());

    }

    @After
    public void tearDown() {
        PeerB.sendMessage("#END#");
        PeerA.close();
        PeerB.close();
    }
    
    public void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
        }
    }
}
