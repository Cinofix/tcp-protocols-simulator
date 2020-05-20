package simulator;

import tcp_protocols.TCPConnection;

public class Segment implements Comparable<Segment>{

    private Integer id;
    private TCPConnection sender;
    private SegmentKind kind;
    private double startTime; // tempo di invio
    private double responseTime; // tempo di timeOut

    public Segment(TCPConnection sender,Integer id,SegmentKind kind){
        this.id = id;
        this.sender = sender;
        this.kind = kind;

        this.startTime = -1;
        this.responseTime = -1;
    }


    @Override
    public int compareTo(Segment o) {
        return Integer.compare(id,o.getId());
    }

    public static enum SegmentKind{
        ACK ("ack"),
        PAYLOAD ("data");

        private String text;

        SegmentKind(String txt){
            this.text = txt;
        }
    }

    public Integer getId() {
        return id;
    }

    public TCPConnection getSender() {
        return sender;
    }

    public SegmentKind getKind() {
        return kind;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setStartTime(double st){
        if(startTime == -1) startTime = st;
    }

    public void calcResponseTime(double rt){
        responseTime = rt - startTime;
    }

}
