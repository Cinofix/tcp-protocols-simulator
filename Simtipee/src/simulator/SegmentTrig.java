package simulator;

public abstract class SegmentTrig implements Trigger{
    private Segment segment;

    public SegmentTrig(Segment seg){
        this.segment = seg;
    }
}
