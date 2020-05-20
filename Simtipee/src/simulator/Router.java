package simulator;

import tcp_protocols.TCPConnection;

import java.util.*;

public class Router implements Receiver {
    private Map<TCPConnection,SortedSet<Integer>> acksFromHost;
    private Main simulation;

    public Router(Main simulation){
        acksFromHost = new HashMap<>();
        this.simulation = simulation;
    }

    @Override
    public void receive(Segment s){
        if(s.getKind().equals(Segment.SegmentKind.PAYLOAD)){

            TCPConnection sender = s.getSender();

            SortedSet<Integer> buffer = acksFromHost.get(sender);
            if(buffer == null) return;

            //System.out.println("Router buffer size: "+buffer.size()+"; Value to add: "+s.getId());

            if(buffer.isEmpty() || s.getId() >= buffer.first() )/* Solo se è maggiore uguale di quello atteso aggiungo */
                buffer.add(s.getId());

            int lastAck = lastConsecutiveAck(buffer);
            SortedSet<Integer> upper = buffer.tailSet(lastAck);
            buffer = new TreeSet<>(upper);
            acksFromHost.put(sender,buffer); /* Update buffer */


            //System.out.println("\u001B[34m"+"Received segment"+s.getId()+" from host "+sender.getSender().getId()+"; Ack_returned: "+lastAck+"\u001B[0m");

            Segment ack = new Segment(sender,lastAck, Segment.SegmentKind.ACK);

            simulation.getChannel().push(sender,ack);
        }
    }


    private int lastConsecutiveAck(SortedSet<Integer> buffer){
        //System.out.println("Buffer: "+buffer.toString());
        int consecutive = buffer.first(); // get the minimum value
        for(Integer id: buffer){
            if(id == consecutive+1)
                consecutive = id;
        }
        return consecutive;
    }

    public void requestConnection(TCPConnection sender){
        acksFromHost.put(sender,new TreeSet<>());
        //System.out.println("Request connection after update buffer: "+acksFromHost.get(sender).toString());
    }

    public void closeConnection(TCPConnection con){
        acksFromHost.remove(con);
    }

    public void clear(){
        acksFromHost = new HashMap<>();
    }
}
