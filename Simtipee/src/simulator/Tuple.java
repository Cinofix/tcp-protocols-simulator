package simulator;

public class Tuple<Double,TriggerAction> implements Comparable<Tuple<Double,TriggerAction>>{
    private double time;
    private TriggerAction trigger;

    public Tuple(double t, TriggerAction a) {
        this.time = t;
        this.trigger = a;
    }

    @Override
    public String toString() {
        return "Pair[" + this.time + "," + this.trigger.toString() + "]";
    }

    public double getTime(){return time;}
    public TriggerAction getAction(){return trigger;}


    @Override
    public int compareTo(Tuple<Double, TriggerAction> o) {
        return java.lang.Double.compare(time,o.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;

        if (java.lang.Double.compare(tuple.time, time) != 0) return false;
        return trigger != null ? trigger.equals(tuple.trigger) : tuple.trigger == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = java.lang.Double.doubleToLongBits(time);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        return result;
    }
}