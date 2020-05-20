package simulator;


public class Statistic {

    private double TIME;

    private long N_TIMEOUT = 0;
    private long N_SEGMENT_FAIL = 0;
    private long N_GOOD_PAYLOAD = 0;
    private long N_SEGMENT = 0;
    private long N_GOOD = 0;
    private long N_DROPPED = 0;

    private Double throughput;


    private int usingChannel;
    private double rstMean,cwMean;
    private double hostsThroughput;
    private double hostX;


    public Statistic(){
        usingChannel = 0;

        TIME = 0;

        N_TIMEOUT = 0;
        N_SEGMENT_FAIL = 0;
        N_GOOD_PAYLOAD = 0;
        N_SEGMENT = 0;
        N_GOOD = 0;
        N_DROPPED = 0;

        hostX = 0;
        hostsThroughput = 0;
        hostX = 0;

    }

    public void increaseTimeout(){ N_TIMEOUT+=1;}
    public void increaseSegmentFail(){ N_SEGMENT_FAIL+=1;}
    public void increaseGoodPayload(){ N_GOOD_PAYLOAD+=1;}
    public void increaseSegment(){N_SEGMENT+=1;}
    public void increaseGood(){N_GOOD+=1;}
    public void increaseDropped(){N_DROPPED+=1;}

    public long getNTimeout() {return N_TIMEOUT;}
    public long getNSegmentFail() {return N_SEGMENT_FAIL;}
    public long getNSegment() {return N_SEGMENT;}
    public long getNGoodPayload() {return N_GOOD_PAYLOAD;}
    public long getNGood() {return N_GOOD;}
    public long getNDropped() {return N_DROPPED;}


    public void updateTime(double t){
        TIME = t;
    }

    public Double getThroughput(){
        throughput = N_GOOD/TIME;
        return throughput;
    }

    public void setHostRTT(double rtt){
        rstMean = rtt;
    }

    public double getRTT(){
        return rstMean;
    }

    public double getCwMean(){
        return cwMean;
    }

    public double getSimulationTime(){ return TIME;}

    public void setUsingChannel(int size){
        usingChannel = size;
    }

    public int getUsingChannel(){ return usingChannel;}

    public void setHostsThroughput(int nSegments){
        hostsThroughput = nSegments/TIME;
    }


    public void setHostThroughput(double x){
        hostX = x;
    }
    public double getHostThroughput(){
        return hostX;
    }



    public Double getHostsThroughput(){
        return hostsThroughput;
    }

    public Statistic extractSample(){
        return new Statistic(this);
    }

    private Statistic(Statistic t){
        N_GOOD = t.getNGood();
        hostsThroughput = t.getHostsThroughput();
        rstMean =  t.getRTT();
        TIME =  t.getSimulationTime();
        N_DROPPED = t.getNDropped();
        hostX = t.getHostThroughput();
    }
}


