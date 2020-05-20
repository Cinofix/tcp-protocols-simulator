package simulator;


import java.util.PriorityQueue;

public class FutureEventList {

    private PriorityQueue<Tuple<Double,Trigger>> fel;

    public FutureEventList(){
        fel = new PriorityQueue<>();
    }

    public void registerAction(Double time,Trigger action){
        fel.add(new Tuple<>(time,action));
    }

    public Tuple<Double,Trigger> next(){
        return fel.poll();
    }

    public int size(){
        return fel.size();
    }

    public void clear(){
        fel.clear();
    }
}
