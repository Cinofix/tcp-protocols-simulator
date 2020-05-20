package tcp_protocols;

import simulator.Host;
import simulator.Main;
import simulator.Segment;

import java.lang.Math;

public class RENO extends TCPConnection {

    private int ack1;
    private int ack2 ;

    public RENO(){super();}

    private RENO(Host host, Main simulation){
        super(host, simulation);
        ssthresh = UPPER_BOUND;
        ack1 = -1;
        ack2 = -2;
    }

    @Override
    public TCPConnection openConnection(Host host, Main simulation) {
        return new RENO(host, simulation);
    }

    @Override
    public boolean timeout(Integer segmentId) {
        //Host sender = this.getSender();
        //System.out.println("\u001B[33mhost: "+sender.getId()+" received timeout of segment"+segmentId+"\u001B[0m");

        if(super.timeout(segmentId)){
            //System.out.println("\u001B[35m"+ "Timeout: "+segmentId +" host: "+ sender.getId()+"\u001B[0m");
            simulation.increaseTimeout();

            ssthresh = Math.max(1, congestionWindow/2);
            congestionWindow = ssthresh; /* TAHOE sets the size to 1 if something went wrong */

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
            /* FAST RECOVERY */
            /*
            if((ack1 == ack2) && (ack2== current_id))
                System.out.println("Ho ricevuto 3 ack uguali di fila: "+current_id);
            */
            if((ack1 == ack2) && (ack2== current_id) && isWaitingAck(current_id+1)){

                ssthresh =  Math.max(1, congestionWindow/2);
                congestionWindow = ssthresh;

                /*
                System.out.println("Recovery for segment: "+current_id);
                System.out.println("re-send: "+segmentsToSend.get(current_id).getId() + " CW: "+congestionWindow);
                */
                invalidateTimeout(current_id+1); // invalida timeout per il successore
                send(segmentsToSend.get(current_id)); // Il successore
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
        if(congestionWindow >= ssthresh) /* Congestion avoidance */
            congestionWindow +=1;
        else
            congestionWindow = Math.min(congestionWindow*2,ssthresh);

        //System.out.println("\u001B[33m"+"*** Host :"+sender.getId()+" success sequence"+" ***\u001B[0m");
        send();
    }

    @Override
    public String toString(){return "RENO";}
}
