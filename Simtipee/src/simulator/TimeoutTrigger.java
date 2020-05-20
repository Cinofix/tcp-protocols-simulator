package simulator;

import tcp_protocols.TCPConnection;


public abstract class TimeoutTrigger implements Trigger {
    private Integer id;
    private TCPConnection connection;

    public TimeoutTrigger(TCPConnection c,Integer id){
        this.id = id;
        connection = c;
    }

    public TCPConnection getConnection(){
        return connection;
    }

    public Integer getId(){ return id;}
    public void invalidate(){
        id = -1;
    }

    public String toString(){
        return "Timeout id: "+id;
    }
}
