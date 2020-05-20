package simulator;

import tcp_protocols.TCPConnection;
import umontreal.ssj.randvar.GeometricGen;
import umontreal.ssj.rng.LFSR113;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Host{
    private int id;
    private final double G;
    private Main simulation;
    private TCPConnection tcpSender;

    private int segmentSended;

    private GeometricGen geomDist;

    public Host(int id, double G, Main simulation){
        this.id = id;
        this.G = G;
        this.simulation = simulation;
        tcpSender = simulation.getProtocol();

        // Seed based on the actual time of execution
        LocalDateTime time = LocalDateTime.now();
        //int seed = (time.getSecond()*23) + (time.getDayOfYear()*13) + (time.getMinute()*19) + 2301;
        int seed = simulation.getSimulationSeed();
        if(seed == 1 || seed == 7 || seed == 15 || seed ==127)
            seed +=1;

        int[] seedToFeed = {seed, seed, seed, seed};

        LFSR113 stream = new LFSR113();
        stream.setSeed(seedToFeed);

        geomDist = new GeometricGen(stream ,G);
    }

    public void start(){
        int segmentToSend = geomDist.nextInt() +1;
        List<Segment> segments = new ArrayList<>();

        tcpSender = tcpSender.openConnection(this, simulation);
        for(int i= 1; i <= segmentToSend; i++)
            segments.add(new Segment(tcpSender,i,Segment.SegmentKind.PAYLOAD));

        tcpSender.prepareSegments(segments);
        //System.out.println("\nSTART Host"+id+" generated "+segmentToSend+" segments... size: "+segments.size());
        simulation.getRouter().requestConnection(tcpSender);
        tcpSender.send();
    }


    public void restart(){
        simulation.getRouter().closeConnection(tcpSender);
        start();
    }

    public void increaseSegmentSended(){
        segmentSended += 1;
    }

    public int getSegmentSended(){ return segmentSended;}

    public int getId(){ return id;}

    public double getMeanRTT(){
        return tcpSender.getMeanRTT();
    }

}
