package simulator;

import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.randvar.UniformGen;
import umontreal.ssj.rng.MRG32k3a;


public class Queue {
    private final double serviceTime;
    private Main simulation;
    private FutureEventList FEL;
    private Router receiver;

    private Integer segmentCount;
    private RandomVariateGen uniformDist;

    public Queue(Main simulation) {
        this.simulation = simulation;
        FEL = simulation.getFutureEventList();
        receiver = simulation.getRouter();
        segmentCount = 0;
        serviceTime = 0.01;

        int seed = simulation.getSimulationSeed();
        long[] seedToFeed = {seed, seed, seed, seed, seed, seed};

        MRG32k3a stream = new MRG32k3a();
        stream.setSeed(seedToFeed);
        uniformDist = new UniformGen(stream);
    }


    public void push(Receiver receiver, Segment s){
        double time;

        simulation.increaseSegment();

        if(segmentCount < simulation.getT()){
            time = simulation.getCurrentTime() + serviceTime +  serviceTime*segmentCount;
            segmentCount+=1;

            //System.out.println("\u001B[33m"+ "Segmenti nel canale: "+segmentCount+"\u001B[0m");

            FEL.registerAction(time,new SegmentTrig(s) {
                @Override
                public void execute() {
                    double value = uniformDist.nextDouble();
                    segmentCount -=1;
                    if(value<=simulation.getP()){
                        //System.out.println("\u001B[34m"+"Success: "+s.getKind()+" id"+s.getId()+", host: "+s.getSender().getSender().getId()+"\u001B[0m");
                        simulation.increaseGood();
                        receiver.receive(s);
                    }else{// else ack non valido
                        //System.out.println("\u001B[31m"+"FAIL: "+s.getKind()+" id"+s.getId()+" by host "+s.getSender().getSender().getId()+" Lost \u001B[0m");
                        simulation.increaseSegmentFail();
                    }
                }
            });
        }else{
            //System.out.println("\u001B[31m"+ "SEGMENT DROPPED FROM THE CHANNEL"+"\u001B[0m");
            simulation.increaseDropped();
        }
    }

    public int getSegmentInChannel(){ return segmentCount;}

}
