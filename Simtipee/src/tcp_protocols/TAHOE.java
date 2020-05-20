package tcp_protocols;

import simulator.Host;
import simulator.Main;
import simulator.Segment;

import java.util.List;

public class TAHOE extends TCPConnection {

    private int ack1;
    private int ack2;

    public TAHOE(){super();}

    private TAHOE(Host host, Main simulation){
        super(host, simulation);
        ack1 = -1;
        ack2 = -1;
    }

    @Override
    public TCPConnection openConnection(Host host, Main simulation) {
        return new TAHOE(host, simulation);
    }

    @Override
    public boolean timeout(Integer segmentId) {
        Host sender = this.getSender();
        //System.out.println("\u001B[33mHost "+sender.getId()+" received timeout of segment"+segmentId+"\u001B[0m");

        if(super.timeout(segmentId)){
            //System.out.println("\u001B[35m"+ "Timeout: "+segmentId +" host: "+ sender.getId()+"\u001B[0m");
            simulation.increaseTimeout();
            ssthresh = Math.max(1, congestionWindow/2);
            congestionWindow = 1; /* TAHOE sets the size to 1 if something went wrong */

            invalidateTimeouts();
            send();
            return true;
        }else{
            //System.out.println("\u001B[35m"+ "Timeout not valid for segment: "+segmentId +" host: "+sender.getId()+"\u001B[0m");
            return false;
        }
    }

    @Override
    public boolean ack(Segment segment) {
        int current_id = segment.getId();

        if(!super.ack(segment)){
            /* FAST RETRANSMIT */

            /*
            if((ack1 == ack2) && (ack2== current_id))
                System.out.println("Ho ricevuto 3 ack uguali di fila: "+current_id);
            */

            if((ack1 == ack2) && (ack2== current_id) && isWaitingAck(current_id+1)){
                ssthresh =  Math.max(1, congestionWindow/2);
                congestionWindow = 1;

                /*
                System.out.println("RETRANSMIT for segment: "+current_id);
                System.out.println("re-send: "+segmentsToSend.get(current_id).getId() + " CW: "+congestionWindow);
                */

                invalidateTimeout(current_id+1); // invalida timeout per il successore
                send(segmentsToSend.get(current_id)); // it isn't current_id +1 because Array start from 0

                /* RESET ack to wait for fast retransmit */
                ack2 = current_id = -1;
            }

            ack1 = ack2;
            ack2 = current_id;
        }
        return true;
    }


    @Override
    public void success() {
        Host sender = this.getSender();
        int tmp = congestionWindow;
        if(congestionWindow >= ssthresh) {
            congestionWindow += 1; /* TAHOE uses slow start to increase the size of the congestion window exponentially */
            //System.out.println(" cw > ssthresh" + congestionWindow +"  "+ ssthresh);
        }else{
            //System.out.println(" cw < ssthresh" + congestionWindow +"  "+ ssthresh);
            congestionWindow = Math.min(congestionWindow*2,ssthresh);
        }

        //System.out.println("\u001B[33m"+"*** Host :"+sender.getId()+" success sequence old: "+tmp+" new: "+congestionWindow+" ***\u001B[0m");
        send();
    }

    @Override
    public String toString(){return "TAHOE";}
}
