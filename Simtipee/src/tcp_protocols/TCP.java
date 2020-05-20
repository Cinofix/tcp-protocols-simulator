package tcp_protocols;

import simulator.Host;
import simulator.Main;
import simulator.Segment;

import java.util.List;

public interface TCP {

    void prepareSegments(List<Segment> segments);
    /* Send segment */
    void send();
    /* Notify the occurrence of timeout for segment */
    boolean timeout(Integer t);
    /* Notify the occurrence of ack for segment */
    boolean ack(Segment segment);
    /* Update congestion window for the connection */
    void success();
    /* Create a new instance of tcp connection */
    TCPConnection openConnection(Host owner, Main simulation);
}
