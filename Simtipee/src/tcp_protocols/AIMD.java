package tcp_protocols;

import simulator.Host;
import simulator.Main;
import simulator.Segment;

public class AIMD extends TCPConnection {

    public AIMD(){super();}

    private AIMD(Host host, Main simulation){
        super(host, simulation);
    }

    @Override
    public TCPConnection openConnection(Host host, Main simulation) {return new AIMD(host, simulation);}

    @Override
    public boolean timeout(Integer segmentId) {
        Host sender = this.getSender();
        //System.out.println("\u001B[33mHost "+sender.getId()+" received timeout of segment "+segmentId+"\u001B[0m");

        if(super.timeout(segmentId)){
            //System.out.println("\u001B[35m"+ "Timeout: "+segmentId +" host: "+ sender.getId()+"\u001B[0m");
            simulation.increaseTimeout();

            congestionWindow= Math.max(1, congestionWindow/2); /* AIMD halves the size of the congestion window if something went wrong */

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
        return super.ack(segment);
    }

    @Override
    public void success() {
        //Host sender = this.getSender();
        //System.out.println("\u001B[33m"+"*** Host: "+sender.getId()+" sequence success"+" ***\u001B[0m");
        congestionWindow+= 1; /* AIMD increases linearly the size of the congestion window */
        send();
    }

    @Override
    public String toString(){return "AIMD";}
}
