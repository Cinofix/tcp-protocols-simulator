package simulator;

import tcp_protocols.TCPConnection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main extends Thread{

    private double TIME = 0;
    private double p_valid;
    private int T;
    private double ERTT;
    private TCPConnection PROTOCOL;
    private Queue channel;
    private final int K;
    private final double G; // valore atteso della geometrica

    private int seed;

    private final FutureEventList FEL = new FutureEventList();
    private Router router;
    private List<Host> hosts = new ArrayList<>();

    private Statistic statistics;
    private Statistic lastStatistics;
    private static boolean KILLED;

    public Main(int host, double p, int buffer, double g, double ertt, TCPConnection protocol,int seed){
        this.K = host;
        this.p_valid = p;
        this.T = buffer;
        this.ERTT = ertt;
        this.PROTOCOL = protocol;
        this.G = g;
        TIME = 0;
        this.seed = seed;

        channel = new Queue(this);
        statistics = new Statistic();
    }

    public double getCurrentTime(){
        return TIME;
    }
    public double getP(){ return p_valid;}
    public int getT(){return T;}
    public double getERTT(){return ERTT;}
    public TCPConnection getProtocol(){return PROTOCOL;}

    public void increaseTimeout(){ statistics.increaseTimeout();}
    public void increaseSegmentFail(){ statistics.increaseSegmentFail();}
    public void increaseGoodPayload(){ statistics.increaseGoodPayload();}
    public void increaseSegment(){ statistics.increaseSegment();}
    public void increaseGood(){ statistics.increaseGood();}
    public void increaseDropped(){ statistics.increaseDropped();}

    public void run(){
        TIME = 0;
        KILLED = false;
        router = new Router(this);
        lastStatistics = null;

        System.out.print("Start time: "+ LocalDateTime.now() + " seed: "+seed);
        LocalDateTime start = LocalDateTime.now();

        for(int i=0; i<K;i++){
            Host h = new Host(i,G, this);
            h.start();
            hosts.add(h);
        }
        while(!KILLED){
        //while(LocalDateTime.now().compareTo(start.plusSeconds(4)) < 0){
            //System.out.println("\n\n=========================================");
            Tuple<Double, Trigger> event = FEL.next();
            TIME = event.getTime();
            //System.out.println("TIME: "+TIME);
            //System.out.println("=========================================\n");
            statistics.updateTime(TIME);
            event.getAction().execute();
            lastStatistics = updateStatistics();

        }

        System.out.println("\n\n=========================================\n");
        System.out.println("Segmenti totali generati: "+statistics.getNTimeout());
        System.out.println("Segmenti persi: "+statistics.getNSegmentFail());

        System.out.println("Segmenti buoni ricevuti: "+statistics.getNGood()+" percentuale sui payload: "+(double)statistics.getNGood()/statistics.getNSegment());


        System.out.println("Timeout scaduti: "+statistics.getNTimeout());
        System.out.println("Dropped PAYLOAD: "+statistics.getNDropped());
        System.out.println("RESIDUAL FEL: "+ FEL.size());

        System.out.println("THROUGHPUT: "+ statistics.getNGood()/TIME+" segm/ms");
    }

    public Statistic updateStatistics(){
        statistics.setUsingChannel(channel.getSegmentInChannel());
        setHostPerformance();
        return statistics.extractSample();
    }
    public Statistic getStatistics(){
        return lastStatistics;
    }


    public FutureEventList getFutureEventList(){
        return FEL;
    }

    public Queue getChannel(){
        return channel;
    }

    public Router getRouter(){
        return router;
    }

    public static void kill(){ KILLED = true;}


    public int getSimulationSeed(){
        return seed;
    }

    public void setHostPerformance(){
        int count = 0;
        double meanRTT = 0.0;
        double hostXmean = 0.0;

        List<Double> hostX = new ArrayList<>();

        for(Host h: hosts) {
            count += h.getSegmentSended();
            meanRTT += h.getMeanRTT();
            hostX.add(h.getSegmentSended()/TIME);
        }
        hostXmean = hostX.stream().mapToDouble(x -> x).average().getAsDouble();

        statistics.setHostThroughput(hostXmean);
        statistics.setHostRTT(meanRTT/hosts.size());
        statistics.setHostsThroughput(count);
    }
}
