package tcp_protocols;

import simulator.*;
import simulator.Queue;

import java.util.*;

public abstract class TCPConnection implements TCP,Receiver {
    private Host sender;
    protected Main simulation;
    protected List<Segment> segmentsToSend;
    private SortedMap<Integer,TimeoutTrigger> waitingAck;

    protected int congestionWindow;

    protected int ssthresh;
    private double timeoutTime;
    private double lastRTT;

    private List<Double> responseTime;
    private double ERTT;

    private int n; // number of segment to send

    //public boolean timeout; // TRUE if there was a timeout signal during the current send action, FALSE otherwise
    private int lastAck; // Id of the last ack
    private int endAck;

    public static final int UPPER_BOUND = 64;

    public Receiver router;


    private static double TIMEOUT_LIMIT = 1000; // 1 secondi

    public TCPConnection(){
        congestionWindow = 1;
        ssthresh = UPPER_BOUND;

        waitingAck = new TreeMap<>();

        ERTT = 0.1;
        responseTime = new ArrayList<>();
        responseTime.add(ERTT);
        lastRTT = ERTT;
        timeoutTime = 2*ERTT;

        lastAck = 0;
        endAck = -1;
    }

    public TCPConnection(Host host, Main simulation){
        sender = host;
        this.simulation = simulation;
        router = simulation.getRouter();

        congestionWindow = 1;
        ssthresh = UPPER_BOUND;

        waitingAck = new TreeMap<>();

        ERTT = simulation.getERTT();
        lastRTT = ERTT;
        responseTime = new ArrayList<>();
        responseTime.add(ERTT);
        timeoutTime = 2*ERTT;

        lastAck = 0;
        endAck = -1;
    }

    public void send(){
        int i;

        //waitingAck.clear();
        for(i = lastAck; i< congestionWindow + lastAck && i<n;i++){
            Segment segment = segmentsToSend.get(i);
            send(segment);
        }
        //System.out.println("CW: "+congestionWindow +"  SSThreshold: "+ssthresh+"   N_size: "+n+"   i: "+i+"   LastAck: "+lastAck);
        endAck = segmentsToSend.get(i-1).getId();

        if(lastAck == n)// id of the last ack of the segments sequence
            sender.restart();
    }

    public void send(Segment segment){

        int segId = segment.getId();
        
        /* Set start time for the segment */
        segment.setStartTime(simulation.getCurrentTime());

        /* Push in channel */
        simulation.getChannel().push(router,segment);

        /* Register TIMEOUT */
        double time = simulation.getCurrentTime() + timeoutTime;
        //System.out.println("ResponseTime send: "+ERTT);
        //System.out.println("TimeoutTime send: "+timeoutTime);

        TimeoutTrigger timeout = new TimeoutTrigger(this,segId) {
            @Override
            public void execute() {
                getConnection().timeout(getId());
            }
        };
        registerAckToWait(segId,timeout);
        simulation.getFutureEventList().registerAction(time,timeout);
    }


    public boolean ack(Segment segment){
        int segId = segment.getId();

        if(isWaitingAck(segId)){ /* Check if the segment is available for the ack signal */
            //System.out.println("\u001B[33m"+"Host: "+sender.getId()+" received ack "+segment.getId()+" \u001B[0m");
            //System.out.println("\u001B[33m"+"CORRECT WAITING"+" \u001B[0m");
            sender.increaseSegmentSended();
            lastAck = Math.max(segId, lastAck); /* Consider only the maximum value of ack because we use cumulative ack */
            removeWaitingAck(segId);

            // update timeoutTime
            timeoutTime = 2*ERTT;
            timeoutTime = Math.min(timeoutTime,TIMEOUT_LIMIT);

            //System.out.println("TimeoutTime for ack: "+timeoutTime);

            if(waitingAck.isEmpty() || lastAck >= endAck){
                success();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean timeout(Integer segmentId) {

        if (isWaitingAck(segmentId)) {
            // update timeoutTime
            timeoutTime = 2*(ERTT*0.9 + 0.1*lastRTT);
            timeoutTime = Math.min(timeoutTime,TIMEOUT_LIMIT);
            //System.out.println("TimeoutTime for timeout: "+timeoutTime);

            return true;
        }
        return false;
    }


    public void calcResponseTime(Segment seg){
        /* Calc response time */
        seg.calcResponseTime(simulation.getCurrentTime());
        //System.out.println("\t\t *** Ack  idAck: "+seg.getId()+ " resp time "+seg.getResponseTime()+ "  ***");
        lastRTT = seg.getResponseTime();
        responseTime.add(lastRTT);
        //System.out.print("\n RESPONSE TIME OLD: "+ERTT+ " lastRTT: "+lastRTT);
        ERTT = 0.9*ERTT + 0.1*lastRTT;
        //System.out.println("   new ResponseTime: "+ERTT);

    }


    public boolean isWaitingAck(Integer id){
        //System.out.println("Host Waiting list:\n\t"+waitingAck.toString());
        boolean ret =  waitingAck.containsKey(id);
        String color = (ret == true ? "\u001B[34m" : "\u001B[31m");
        //System.out.println("\tSearching: "+color+id+"\u001B[0m ret: "+color+ret+"\u001B[0m");
        return ret;
    }

    public void registerAckToWait(Integer id,TimeoutTrigger t) {
        waitingAck.put(id,t);
    }

    public void removeWaitingAck(Integer id){
        SortedMap<Integer,TimeoutTrigger> head = waitingAck.headMap(id+1); // we are using id+1 because headMap return all values strictly less than id
        //System.out.println("Waiting List before:\n\t"+waitingAck.toString());

        waitingAck = waitingAck.tailMap(id+1); // we are using id+1 because tailMap return all values grater or equal than id

        /* Invalida i timeout precedenti */
        for(Map.Entry<Integer,TimeoutTrigger> entry: head.entrySet()){
            entry.getValue().invalidate();
            simulation.increaseGoodPayload();

            calcResponseTime(segmentsToSend.get(entry.getKey()-1));
        }
        //System.out.println("Waiting List after removal:\n\t"+waitingAck.toString());
    }

    public void invalidateTimeouts(){
        for(Map.Entry<Integer,TimeoutTrigger> entry: waitingAck.entrySet())
            entry.getValue().invalidate();
    }

    public void invalidateTimeout(Integer segmentId){
        TimeoutTrigger tt = waitingAck.remove(segmentId);
        if(tt != null) tt.invalidate();
    }

    public Host getSender(){ return sender;}

    @Override
    public void receive(Segment s) {
        if(s.getKind().equals(Segment.SegmentKind.ACK))
            ack(s);
    }

    @Override
    public void prepareSegments(List<Segment> segments){
        segmentsToSend = segments;
        n = segmentsToSend.size();
    }

    public double getMeanRTT(){
        double mean = 0.0;

        for(Double rsp: responseTime)
            mean+=rsp;
        return mean/responseTime.size();
    }

}
